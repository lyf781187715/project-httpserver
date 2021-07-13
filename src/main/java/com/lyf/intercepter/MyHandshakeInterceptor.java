package com.lyf.intercepter;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Component
public class MyHandshakeInterceptor implements HandshakeInterceptor {


    //握手之前执行，是否继续握手
    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse,
                                   WebSocketHandler webSocketHandler, Map<String, Object> attributes) throws Exception {
        ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) serverHttpRequest;

        HttpSession session = servletServerHttpRequest.getServletRequest().getSession();
        String meetingId = servletServerHttpRequest.getServletRequest().getParameter("meetingId");
        String userId = servletServerHttpRequest.getServletRequest().getParameter("userId");


        String modelType = servletServerHttpRequest.getServletRequest().getParameter("modelType");




        if(meetingId != null) {
            session.setAttribute("meetingId", meetingId);
            attributes.put("meetingId", meetingId);
        }
        if(userId != null){
            session.setAttribute("userId", userId);
            attributes.put("userId", userId);
        }
        if(modelType != null){
            session.setAttribute("modelType", modelType);
            attributes.put("modelType",modelType);
        }else{
            session.setAttribute("modelType", "4");
            attributes.put("modelType","4");
        }

        //此处可以添加权限认证并return false
//        if(session!=null){
//            String userName = (String) session.getAttribute("userName");
//            if(userName!=null){
//                session.setAttribute("userName",userName);
//                attributes.put("userName",userName);
//            }else {
//                session.setAttribute("userName",session.getId());
//                session.setAttribute("userName",userName);
//            }
//        }

        System.out.println("开始握手");
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

        System.out.println("握手成功了！～！！！");
    }


}
