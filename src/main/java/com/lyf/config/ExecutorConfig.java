package com.lyf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {


    @Bean("threadPool")
    public Executor threadPool(){
        return Executors.newFixedThreadPool(10);
    }
}
