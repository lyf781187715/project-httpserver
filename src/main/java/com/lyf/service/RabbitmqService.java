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



    private TranslateService translateService;
    private final Meeting meeting;
    private Connection connection;

    private int log_id = -1;
    private String his = "";
    private String lastSrc = "";
    private String status = "1";

    public RabbitmqService(Meeting meeting,TranslateService translateService,Connection connection) {
        this.meeting = meeting;
        this.translateService = translateService;
        this.connection = connection;
    }

    @Override
    public void run() {
        String meetingId = meeting.getMeetingId()+"";
        int modelType = meeting.getModelType();
        String type = "1";
        int newLog_id = 0;
        Channel channel = null;
        try {
            channel = connection.createChannel();

            while(!Thread.currentThread().isInterrupted()){
                GetResponse getResponse = channel.basicGet(meetingId,true);
                if(getResponse==null){
                    //Thread.sleep(2000);
                    continue;
                }else{
                    String json = new String(getResponse.getBody());
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, String> map = objectMapper.readValue(json, HashMap.class);
                    type = map.get("type");
                    //System.out.println("------type:"+type);
                    //System.out.println("first"+log_id);
                    newLog_id = Integer.parseInt(map.get("seq"));
                    while(type.equals("1") && newLog_id == log_id){
                        getResponse = channel.basicGet(meetingId,true);
                        //System.out.println("进入逻辑");
                        //System.out.println("second"+log_id);
                        if(getResponse==null){
                            break;
                        }
                        json = new String(getResponse.getBody());
                        map = objectMapper.readValue(json, HashMap.class);
                        log_id = newLog_id;
                        newLog_id = Integer.parseInt(map.get("seq"));
                        System.out.println("third"+log_id);
                        type = map.get("type");
                    }
                    String text = map.get("text");
                    log_id = Integer.parseInt(map.get("seq"));
                    //System.out.println("消息队列消息"+text);
                    //sendMessageToGroup(meetingId,new TextMessage(message));
                    String extra_info = "1";
                    String tranRes = "";
                    int lastLength = lastSrc.split(" ").length;
                    //System.out.println("lastLength"+lastLength);
                    int nowLength = text.split(" ").length;
                    //System.out.println("nowLength"+nowLength);
                    if(type.equals("2") || lastLength>nowLength && lastSrc.charAt(0)!=text.charAt(0)){
                        if(type.equals("2")){
                            extra_info = "0";
                        }
                        his = "";
                    }
                    Translate translate = new Translate(String.valueOf(log_id),1,modelType,text,his,extra_info);


                    TranslateResp translateResp = translateService.sendPost(translate);

                    JSONObject res = new JSONObject();
                    res.put("seq",String.valueOf(log_id));
                    status = String.valueOf(translateResp.getStatus());
                    res.put("status",status);
                    if(translateResp.getStatus()==0) {
                        his = tranRes;
                        lastSrc = translateResp.getSrc();
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
                        res.put("src","");
                        res.put("tranRes","翻译模型不可用");
                        sendMessageToGroup(meetingId,new TextMessage(res.toJSONString()));
                    }else if(translateResp.getStatus()==1002){
                        res.put("src","");
                        res.put("tranRes","翻译失败");
                        sendMessageToGroup(meetingId,new TextMessage(res.toJSONString()));
                   }else if(translateResp.getStatus()==1000){
                        res.put("src","");
                        res.put("tranRes","翻译方向不可用");
                        sendMessageToGroup(meetingId,new TextMessage(res.toJSONString()));
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
