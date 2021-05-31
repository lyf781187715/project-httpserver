package com.lyf.service;


import com.alibaba.fastjson.JSONObject;
import com.lyf.handler.SessionManager;
import com.lyf.pojo.Meeting;
import com.lyf.pojo.Translate;
import com.lyf.pojo.TranslateResp;
import com.rabbitmq.client.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class RabbitmqService implements Runnable {



    private TranslateService translateService;
    private final Meeting meeting;
    private Connection connection;

;

    private int log_id = 0;
    String his = "";
    String lastSrc = "";

    public RabbitmqService(Meeting meeting,TranslateService translateService,Connection connection) {
        this.meeting = meeting;
        this.translateService = translateService;
        this.connection = connection;
    }

    @Override
    public void run() {

        String meetingId = meeting.getMeetingId()+"";

        Channel channel = null;
        try {
            channel = connection.createChannel();
            while(!Thread.currentThread().isInterrupted()){
                GetResponse getResponse = channel.basicGet(meetingId,true);
                if(getResponse==null){
                    Thread.sleep(2000);
                }else{
                    String message = new String(getResponse.getBody());
                    System.out.println("来自消息队列的消息" + message);
                    //sendMessageToGroup(meetingId,new TextMessage(message));


                    String extra_info = "";
                    String tranRes = "";
                    int lastLength = lastSrc.split(" ").length;
                    //System.out.println("lastLength"+lastLength);
                    int nowLength = message.split(" ").length;
                    //System.out.println("nowLength"+nowLength);
                    if(lastLength>nowLength && lastSrc.charAt(0)!=message.charAt(0)){
                        log_id++;
                        his = "";
                    }
                    Translate translate = new Translate(String.valueOf(log_id),meeting.getDirect(),0,message,his,extra_info);

                    TranslateResp translateResp = translateService.sendPost(translate);
                    if(translateResp.getStatus()==0) {
                        his = tranRes;
                        lastSrc = translateResp.getSrc();
                        //System.out.println("-------->"+lastSrc);
                        if(translateResp.getTrans_act()==1){
                            JSONObject res = new JSONObject();
                            tranRes = translateResp.getTrans_res();
                            res.put("src",translateResp.getSrc());
                            res.put("tranRes",tranRes);
                            sendMessageToGroup(meetingId,new TextMessage(res.toJSONString()));
                        }

                    }else if(translateResp.getStatus()==1001){
                        sendMessageToGroup(meetingId,new TextMessage("翻译方向不可用"));
                    }else if(translateResp.getStatus()==1002){
                        sendMessageToGroup(meetingId,new TextMessage("翻译失败"));
                    }else{
                        sendMessageToGroup(meetingId,new TextMessage("Someting wrong"));
                    }

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
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
