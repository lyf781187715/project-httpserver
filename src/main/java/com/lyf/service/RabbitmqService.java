package com.lyf.service;


import com.lyf.handler.SessionManager;
import com.lyf.handler.WebsocketHandler;
import com.lyf.utils.RabbitMqUtils;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitmqService implements Runnable {

    private String meetingId;

    private int sentenceId;


    public RabbitmqService(String meetingId) {
        this.meetingId = meetingId;
    }

    @Override
    public void run() {
        Connection connection = null;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("47.106.132.112");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setVirtualHost("/");
        Channel channel = null;
        try {
            connection = factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        try {
            channel = connection.createChannel();
            while(!Thread.currentThread().isInterrupted()){
                channel.basicConsume(meetingId, true, new DeliverCallback() {
                    @Override
                    public void handle(String s, Delivery delivery) throws IOException {


                        String message = new String(delivery.getBody(),"UTF-8");
                        System.out.println("来自消息队列的消息" + message);

                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sendMessageToGroup(meetingId,new TextMessage(message));
                        //这里进行分发逻辑，也就是后来的向model发和接受逻辑
                    }
                }, new CancelCallback() {
                    @Override
                    public void handle(String s) throws IOException {
                        System.out.println("接收消息失败");
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
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
        //ArrayList<WebSocketSession> userList = userMap.get(meetingId);

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
