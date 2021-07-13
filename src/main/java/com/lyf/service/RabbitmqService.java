package com.lyf.service;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.handler.SessionManager;
import com.lyf.pojo.Meeting;
import com.lyf.pojo.Translate;
import com.lyf.pojo.TranslateResp;
import com.lyf.utils.RabbitMqUtils;
import com.rabbitmq.client.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitmqService implements Runnable {

    String modelType = "4";


    private TranslateService translateService;
    private final Meeting meeting;
    private Connection connection;

    private int log_id = 0;
    private String his = "";
    private String lastSrc = "";
    private String status = "1";

    public RabbitmqService(Meeting meeting,TranslateService translateService,Connection connection,String modeltype) {
        this.meeting = meeting;
        this.translateService = translateService;
        this.connection = connection;
        this.modelType = modeltype;
    }

    @Override
    public void run() {
        String meetingId = meeting.getMeetingId()+"";
        String type = "1";
        Channel channel = null;
        try {
            channel = connection.createChannel();

            while(!Thread.currentThread().isInterrupted()){
                GetResponse getResponse = channel.basicGet(meetingId,true);
                if(getResponse==null){
                    Thread.sleep(2000);
                }else{
                    String json = new String(getResponse.getBody());
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, String> map = objectMapper.readValue(json, HashMap.class);
                    log_id = Integer.parseInt(map.get("seq"));
                    while(log_id!=0 && type == "1"){
                        getResponse = channel.basicGet(meetingId,true);
                        if(getResponse==null){
                            break;
                        }
                        json = new String(getResponse.getBody());
                        map = objectMapper.readValue(json, HashMap.class);
                        type = map.get("type");
                    }

                    String text = map.get("text");
                    System.out.println("消息队列消息"+text);
                    //sendMessageToGroup(meetingId,new TextMessage(message));
                    String extra_info = "1";
                    String tranRes = "";
                    int lastLength = lastSrc.split(" ").length;
                    //System.out.println("lastLength"+lastLength);
                    int nowLength = text.split(" ").length;
                    //System.out.println("nowLength"+nowLength);
                    if(type == "2" || lastLength>nowLength && lastSrc.charAt(0)!=text.charAt(0)){
                        if(type == "2"){
                            extra_info = "0";
                        }
                        log_id++;
                        his = "";
                    }
                    Translate translate = new Translate(String.valueOf(log_id),1,Integer.parseInt(modelType),text,his,extra_info);

                    TranslateResp translateResp = translateService.sendPost(translate);


                    JSONObject res = new JSONObject();
                    res.put("seq",log_id);
                    status = String.valueOf(translateResp.getStatus());
                    res.put("status",status);
                    if(translateResp.getStatus()==0) {
                        his = tranRes;
                        lastSrc = translateResp.getSrc();
                        //System.out.println("-------->"+lastSrc);
                        if(translateResp.getTrans_act()==1){
                            //JSONObject res = new JSONObject();
                            tranRes = translateResp.getTrans_res();
                            //res.put("seq",seq);
                            res.put("src",translateResp.getSrc());
                            res.put("tranRes",tranRes);
                            sendMessageToGroup(meetingId,new TextMessage(res.toJSONString()));
                        }

                    }
                    else if(translateResp.getStatus()==1001){
                        res.put("src",translateResp.getSrc());
                        sendMessageToGroup(meetingId,new TextMessage("翻译方向不可用"));
                    }else if(translateResp.getStatus()==1002){
                        res.put("src",translateResp.getSrc());
                        sendMessageToGroup(meetingId,new TextMessage("翻译失败"));
                   }
                else{
                        res.put("src","");
                        res.put("tranRes","Someting wrong");
                        sendMessageToGroup(meetingId,new TextMessage(res.toJSONString()));
                    }

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.interrupted();
        } catch (HttpClientErrorException e){
            e.printStackTrace();
            JSONObject res = new JSONObject();
            res.put("status","500");
            res.put("tranRes","翻译服务器现在关闭状态");
            sendMessageToGroup(meetingId,new TextMessage(res.toJSONString()));
        }
        finally {
            RabbitMqUtils.clearQueue(connection,meetingId+"");
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null && connection.isOpen()) {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    public void sendMessageToGroup(String meetingId,TextMessage message) {
        ArrayList<WebSocketSession> userList = SessionManager.getList(meetingId);
        if (userList != null && userList.size() > 0) {
            for (WebSocketSession user : userList) {
                if (user.isOpen()) {
                    try {
                        user.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("给用户" + user.getAttributes().get("userId") + "发送失败");
                    }

                }
            }
        }
    }
}
