package com.lyf.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.pojo.Translate;
import com.lyf.pojo.TranslateResp;
import com.lyf.pojo.User;
import com.lyf.service.TranslateService;
import com.lyf.service.UserServiceimpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserServiceimpl userServiceimpl;

    @Autowired
    TranslateService translateService = new TranslateService();

    @ResponseBody
    @PostMapping("/login")
    public String login(@RequestBody User user) throws JsonProcessingException {
        //System.out.println(pwd);


        User searchUser = userServiceimpl.queryUser(user.getUserName(),user.getUserPwd());
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();
        if(user != null){
            map.put("state",0);
            map.put("userId",searchUser.getUserId());
        }else{
            map.put("state",1);
            map.put("errorMessage","wrong username or password");
        }
        String str = mapper.writeValueAsString(map);
        return str;

    }
    @ResponseBody
    @PostMapping("/signup")
    public String newUser(@RequestBody User user) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();
        if(userServiceimpl.queryUserByName(user.getUserName())==null){
            userServiceimpl.addUser(user);
            User searchUser = userServiceimpl.queryUser(user.getUserName(),user.getUserPwd());
            map.put("state",0);
            map.put("userId",searchUser.getUserId());
        }else{
            map.put("state",1);
            map.put("errorMessage","User already existed");
        }
        String str = mapper.writeValueAsString(map);
        return str;

    }

    @ResponseBody
    @PostMapping("/updateUser")
    public String updateUser(@RequestBody User user) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();
        userServiceimpl.updateUser(user);
        map.put("state",0);
        String str = mapper.writeValueAsString(map);
        return str;

    }
    @ResponseBody
    @PostMapping("/deleteUser")
    public String deleteUser(@RequestBody User user) throws JsonProcessingException {
        userServiceimpl.deleteUser(user.getUserName());
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("state",0);
        String str = mapper.writeValueAsString(map);
        return str;

    }

//    @ResponseBody
//    @PostMapping("/test")
//    public String test(@RequestBody Translate translate){
//        System.out.println(translate.toString());
//
//        return "{\"log_id\":\"10\",\n" +
//                " \"status\":0,\n"+
//                " \"model_type\":0,\n" +
//                " \"src\":\"what are we doing\",\n" +
//                " \"trans_res\":\"???????????????\",\n" +
//                " \"trans_act\":1\"\"\n" +
//                "}";
//    }
//
//    @Autowired
//    RestTemplate restTemplate;

//    @ResponseBody
//    @PostMapping("/testfortest")
//    public String testfortest(@RequestBody Translate translate) {
//        ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://localhost:8080/test",
//                translate, String.class);
//        String body = responseEntity.getBody();
//        return body;
//
//    }

//    @ResponseBody
//    @PostMapping("/t")
//    public String t() {
//        int log_id = 2;
//
//        Translate translate = new Translate(String.valueOf(log_id),2,0,"send","1","1");
//        TranslateResp translateResp = translateService.sendPost(translate);
//        return translateResp.toString();
//
//    }

}
