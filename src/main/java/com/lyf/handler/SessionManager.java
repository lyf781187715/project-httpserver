package com.lyf.handler;


import org.springframework.web.socket.WebSocketSession;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class SessionManager {
    private static ConcurrentHashMap<String, ArrayList<WebSocketSession>> userMap = new ConcurrentHashMap<>();


    public static void addSession(String meetingId,WebSocketSession session){
        ArrayList<WebSocketSession> users;
        if(userMap.containsKey(meetingId)){
            users = userMap.get(meetingId);
        }else {
            users = new ArrayList<>();
        }
        users.add(session);
        userMap.put(meetingId, users);
    }
    public static void remove(String meetingId){
        userMap.remove(meetingId);
    }

    public static void update(String meetingId,ArrayList<WebSocketSession> userList){
        userMap.put(meetingId,userList);
    }
    public static ArrayList<WebSocketSession> getList(String meetingId){
        return userMap.get(meetingId);
    }

    public static boolean isContains(String meetingId){
        return userMap.containsKey(meetingId);
    }
}
