package com.lyf.handler;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.pojo.Meeting;
import com.lyf.pojo.Translate;
import com.lyf.pojo.TranslateResp;
import com.lyf.service.FileService;
import com.lyf.service.MeetingServiceimpl;
import com.lyf.service.TranslateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WebsocketHandler extends TextWebSocketHandler {
    //储存分组信息 meetingId：用户 list
    private static final Map<String, ArrayList<WebSocketSession>> userMap = new HashMap<>();

    private static volatile Map<String,Boolean> userFlag = new HashMap<>();

    private static int log_id=0;

    @Autowired
    FileService fileService = new FileService();

    @Autowired
    TranslateService translateService = new TranslateService();

    @Autowired
    MeetingServiceimpl meetingServiceimpl;

    @Value(value = "${filepath}")
    private String filepath;



    //接受端信息，并发出
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String meetingId = (String) session.getAttributes().get("meetingId");

        System.out.println("收到用户"+userId+"发来的消息");

        Meeting meeting = meetingServiceimpl.queryMeetingById(Integer.valueOf(meetingId));

        //要在这里调用语音符文并转化为text发给meeting中所有成员。

        //if(userFlag.get(meetingId)){
        //    //写入并请求获得翻译
        //    userFlag.put(meetingId,false);
        //    fileService.writeFile(meetingId,message.getPayload(),true,true);

        //    String extra_info = "";
        //    String his = fileService.readFile(meetingId,false);

        //    Translate translate = new Translate(String.valueOf(log_id),meeting.getDirect(),0,message.getPayload(),his,extra_info);
        //    TranslateResp translateResp = translateService.sendPost(translate);
        //    if(translateResp.getStatus()==0) {
        //        log_id++;
        //        if(translateResp.getTrans_act()==1){
        //            fileService.writeFile(meetingId,translateResp.getTrans_res(),false,false);
        //            JSONObject res = new JSONObject();
        //            res.put("src",translateResp.getSrc());
        //            res.put("his",translateResp.getTrans_res());
        //            sendMessageToGroup(meetingId,new TextMessage(res.toJSONString()));
        //        }

        //    }else if(translateResp.getStatus()==1001){
        //        sendMessageToGroup(meetingId,new TextMessage("翻译方向不可用"));
        //    }else if(translateResp.getStatus()==1002){
        //        sendMessageToGroup(meetingId,new TextMessage("翻译失败"));
        //    }else{
        //        sendMessageToGroup(meetingId,new TextMessage("Someting wrong"));
        //    }
        //    userFlag.put(meetingId,true);
        //}else{
        //    //只写入
        //    fileService.writeFile(meetingId,message.getPayload(),true,true);
        //}

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

        String json = message.getPayload();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = objectMapper.readValue(json, HashMap.class);
        String text = map.get("text");
        String type = map.get("type");
        System.out.println(type+": " + text);

        // implement write message to file for now
        if (type.equals("2")) { // end of a sentence
            fileService.writeFile(meetingId, text, true, true);
        }

        sendMessageToGroup(meetingId,new TextMessage(text));
    }

    //建立连接后处理，离线消息推送
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String meetingId = (String) session.getAttributes().get("meetingId");
        Map uM = userMap;
        ArrayList<WebSocketSession> users;
        if(userMap.containsKey(meetingId)){
            users = userMap.get(meetingId);
        }else {
            users = new ArrayList<>();
        }

        if(!userFlag.containsKey(meetingId)){
            userFlag.put(meetingId,true);
        }

        users.add(session);
        userMap.put(meetingId, users);
        session.sendMessage(new TextMessage(userId+"加入会议"+meetingId));
        System.out.println("sent enter msg");
    }


    //关闭连接后处理
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String)session.getAttributes().get("userId");
        String meetingId = (String)session.getAttributes().get("meetingId");
        Map uM = userMap;

        Meeting meeting = meetingServiceimpl.queryMeetingById(Integer.valueOf(meetingId));
        if (String.valueOf(meeting.getUserId()).equals(userId) ){
            System.out.println("speaker has left");
            // implement broadcast speaker has left message
            ArrayList<WebSocketSession> userList = userMap.get(meetingId);
            for (WebSocketSession session1 : userList){
                if (session1.isOpen()) {
                    System.out.println(session1.getUri());
                    session1.sendMessage(new TextMessage("001SYSTEM MESSAGE: SPEAKER HAS LEFT"));
                }
            }
        }
        System.out.println(userId+" has left "+meetingId);

        ArrayList<WebSocketSession> userList = userMap.get(meetingId);
        userList.remove(session);
        userMap.remove(meetingId); // remove the existing meeting
        userMap.put(meetingId, userList); // update the userList in userMap
        if(userList.size()<=0){ // if no more users in meeting, remove the meeting
            userMap.remove(meetingId);
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
        ArrayList<WebSocketSession> userList = userMap.get(meetingId);
        // todo: check logic of userList being null
        if (userList != null) {
            userList.remove(session);
        }

    }


    public void sendMessageToGroup(String meetingId,TextMessage message){
        ArrayList<WebSocketSession> userList = userMap.get(meetingId);
        if(userList!=null&&userList.size()>0){
            for (WebSocketSession user:userList) {
//                System.out.println(user.getAttributes().get("userName"));
//                System.out.println(user.getAttributes().get("meetingId"));
                if(user.isOpen()){
                    try{
                        user.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("给用户"+user.getAttributes().get("userId")+"发送失败");
                    }

                }
            }
        }


    }
}
