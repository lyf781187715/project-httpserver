package com.lyf.config;

import com.lyf.intercepter.MyHandshakeInterceptor;
import com.lyf.handler.WebsocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {


    @Resource
    private MyHandshakeInterceptor myHandshakeInterceptor;


    @Bean
    public WebsocketHandler websocketHandler(){return new WebsocketHandler();}
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry
                .addHandler(websocketHandler(),"/ws")
                .setAllowedOrigins("*")
                .addInterceptors(myHandshakeInterceptor);
    }
}
