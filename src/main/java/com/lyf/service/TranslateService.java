package com.lyf.service;

import com.alibaba.fastjson.JSON;
import com.lyf.pojo.Translate;
import com.lyf.pojo.TranslateResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TranslateService {
    @Value(value = "${translateserver.url}")
    private String translateServerUrl;

    @Autowired
    private RestTemplate restTemplate;

    public HttpEntity<String> generatePostJson(Translate translate){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//        Map<String,Object> jsonMap = new HashMap<>();
//        jsonMap.put("log_id",translate.getLog_id());
//        jsonMap.put("direct",translate.getDirect());
//        jsonMap.put("model_type",translate.getModel_type());
//        jsonMap.put("src",translate.getSrc());
//        jsonMap.put("history",translate.getHistory());
//        jsonMap.put("extra_info",translate.getExtra_info());
//        HttpEntity<Map<String,Object>> httpEntity = new HttpEntity<>(jsonMap,httpHeaders);
        String body = JSON.toJSONString(translate);
        HttpEntity httpEntity = new HttpEntity(body,httpHeaders);
        //System.out.println(httpEntity.toString());
        return httpEntity;
    }

    public TranslateResp sendPost(Translate translate){

        //System.out.println("开始发送了");
        String strBody = restTemplate.postForEntity(
                translateServerUrl,
                generatePostJson(translate),
                String.class
        ).getBody();
        TranslateResp translateResp = JSON.parseObject(strBody, TranslateResp.class);
        //System.out.println(translateResp.toString());
        return translateResp;
    }
}
