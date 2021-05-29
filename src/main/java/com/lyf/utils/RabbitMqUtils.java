package com.lyf.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class RabbitMqUtils {
    @Value("${spring.rabbitmq.host}")
    private  String host;

    @Value("${spring.rabbitmq.port}")
    private  int port;

    @Value("${spring.rabbitmq.username}")
    private  String userName;

    @Value("${spring.rabbitmq.password}")
    private String passWord;

    public  Connection getConnection(){

        Connection connection = null;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            factory.setUsername(userName);
            factory.setPassword(passWord);
            factory.setVirtualHost("/");
            connection = factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public void creatQueue(Connection connection,String name) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            channel.queueDeclare(name, false, false, false, null);
        } catch (IOException e) {
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
}
