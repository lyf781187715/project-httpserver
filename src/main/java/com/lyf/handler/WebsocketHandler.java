package com.lyf.handler;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.pojo.Meeting;
import com.lyf.pojo.Translate;
import com.lyf.pojo.TranslateResp;
import com.lyf.service.FileService;
import com.lyf.service.MeetingServiceimpl;
import com.lyf.service.RabbitmqService;
import com.lyf.service.TranslateService;
import com.lyf.utils.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Component
public class WebsocketHandler extends TextWebSocketHandler {

    //储存分组信息 meetingId：用户 list
    //private  Map<String, ArrayList<WebSocketSession>> userMap = new HashMap<>();

    private Map<String,Thread> threadMap = new ConcurrentHashMap<>();


    @Autowired
    FileService fileService = new FileService();

    @Autowired
    TranslateService translateService = new TranslateService();

    @Autowired
    MeetingServiceimpl meetingServiceimpl;

    @Autowired
    RabbitMqUtils rabbitMqUtils;

    @Value(value = "${filepath}")
    private String filepath;


    //接受端信息，并发出
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String meetingId = (String) session.getAttributes().get("meetingId");

        System.out.println("收到用户"+userId+"发来的消息");

        Meeting meeting = meetingServiceimpl.queryMeetingById(Integer.valueOf(meetingId));

        //rabbitMQ part

        Connection connection = rabbitMqUtils.getConnection();
        Channel channel = connection.createChannel();
        channel.basicPublish("",meetingId,null,message.getPayload().getBytes());


        // deletes the last line if new line the is immediate incremental (too complicated)
        //File f = new File(filepath+"/"+meetingId+"_src");
        //if (f.exists() && !f.isDirectory()) { // doesn't read from first time
        //    String lastLine = fileService.readFileLast(meetingId, true);
        //    int lastLineLength = lastLine.split(" ").length;
        //    String newLine = message.getPayload();
        //    int newLineLength = newLine.split(" ").length;
        //    if (newLineLength >= lastLineLength){
        //        fileService.removeLine(meetingId, true);
        //    } else {
        //        if (newLineLength > 2){
        //            fileService.removeLine(meetingId, true);
        //        }
        //    }
        //}

//        String json = message.getPayload();
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, String> map = objectMapper.readValue(json, HashMap.class);
//        String text = map.get("text");
//        String type = map.get("type");
//        System.out.println(type+": " + text);

        // implement write message to file for now
//        if (type.equals("2")) { // end of a sentence
//            fileService.writeFile(meetingId, text, true, true);
//        }

        //sendMessageToGroup(meetingId,new TextMessage(text));
    }

    //建立连接后处理，离线消息推送
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String meetingId = (String) session.getAttributes().get("meetingId");

        ArrayList<WebSocketSession> users;
        Meeting meeting = meetingServiceimpl.queryMeetingById(Integer.valueOf(meetingId));

        if(!SessionManager.isContains(meetingId)){
            Connection connection = rabbitMqUtils.getConnection();
            RabbitmqService rabbitmqService = new RabbitmqService(meeting,translateService,connection);
            Thread thread = new Thread(rabbitmqService);
            threadMap.put(meetingId,thread);
            thread.start();
        }

        SessionManager.addSession(meetingId,session);
//        if(userMap.containsKey(meetingId)){
//            users = userMap.get(meetingId);
//        }else {
//            users = new ArrayList<>();
//            RabbitmqService rabbitmqService = new RabbitmqService(meetingId);
//            Thread thread = new Thread(rabbitmqService);
//            threadMap.put(meetingId,thread);
//            thread.start();
//        }
//
//
//        users.add(session);
//        userMap.put(meetingId, users);
        session.sendMessage(new TextMessage(userId+"加入会议"+meetingId));
        System.out.println("sent enter msg");
    }


    //关闭连接后处理
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String)session.getAttributes().get("userId");
        String meetingId = (String)session.getAttributes().get("meetingId");


        Meeting meeting = meetingServiceimpl.queryMeetingById(Integer.valueOf(meetingId));
        if (String.valueOf(meeting.getUserId()).equals(userId) ){
            System.out.println("speaker has left");
            // implement broadcast speaker has left message
            //ArrayList<WebSocketSession> userList = userMap.get(meetingId);
            ArrayList<WebSocketSession> userList = SessionManager.getList(meetingId);
            for (WebSocketSession session1 : userList){
                if (session1.isOpen()) {
                    System.out.println(session1.getUri());
                    session1.sendMessage(new TextMessage("001SYSTEM MESSAGE: SPEAKER HAS LEFT"));
                }
            }
        }
        System.out.println(userId+" has left "+meetingId);

        //ArrayList<WebSocketSession> userList = userMap.get(meetingId);
        ArrayList<WebSocketSession> userList = SessionManager.getList(meetingId);
        userList.remove(session);
        SessionManager.remove(meetingId); // remove the existing meeting
        SessionManager.update(meetingId, userList); // update the userList in userMap
        if(userList.size()<=0){ // if no more users in meeting, remove the meeting
            SessionManager.remove(meetingId);
            threadMap.get(meetingId).interrupt();
        }
    }

    //抛出异常时处理
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if(session.isOpen()){
            session.close();
        }
        String userId = (String)session.getAttributes().get("userId");
        String meetingId = (String)session.getAttributes().get("meetingId");
        //ArrayList<WebSocketSession> userList = userMap.get(meetingId);
        ArrayList<WebSocketSession> userList = SessionManager.getList(meetingId);
        // todo: check logic of userList being null
        if (userList != null) {
            userList.remove(session);
        }

    }

//    public void sendMessageToGroup(String meetingId,TextMessage message){
//        //ArrayList<WebSocketSession> userList = userMap.get(meetingId);
//
//        ArrayList<WebSocketSession> userList = SessionManager.getList(meetingId);
//        if(userList!=null&&userList.size()>0){
//            for (WebSocketSession user:userList) {
////                System.out.println(user.getAttributes().get("userName"));
////                System.out.println(user.getAttributes().get("meetingId"));
//                if(user.isOpen()){
//                    try{
//                        user.sendMessage(message);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        System.out.println("给用户"+user.getAttributes().get("userId")+"发送失败");
//                    }
//
//                }
//            }
//        }
//
//
//    }
}
