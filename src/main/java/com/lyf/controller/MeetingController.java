package com.lyf.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.pojo.Meeting;
import com.lyf.pojo.User;
import com.lyf.service.FileService;
import com.lyf.service.MeetingServiceimpl;
import com.lyf.service.UserServiceimpl;
import com.lyf.utils.RabbitMqUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.rabbitmq.client.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MeetingController {

    @Autowired
    FileService fileService = new FileService();

    @Autowired
    private MeetingServiceimpl meetingServiceimpl;

    @Autowired
    private UserServiceimpl userServiceimpl;

    @Autowired
    RabbitMqUtils rabbitMqUtils;

    @Value(value = "${socketserver.url}")
    private String socketurl;


    @ResponseBody
    @PostMapping("/startMeeting")
    //这个方法会收到一个密码，然后返回一个meeting的id，将id密码写入数据库，并重定向到socketserver
    public String newMeeting(@RequestBody Meeting meeting) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();

        User user = userServiceimpl.queryUserByName(meeting.getUserName());


        try{
            int meetingId = meetingServiceimpl.creatNewId();
            Meeting meeting1 = new Meeting(meetingId,
                    meeting.getPwd(),meeting.getRoomTitle(),meeting.getUserName(),
                    meeting.getRoomDescription(),user.getUserId(),meeting.getDirect(), meeting.getImageUrl(),
                    1,
                    meeting.getModelType());
            // start meeting default set status to 1
            meetingServiceimpl.addMeeting(meeting1);

            Connection connection = rabbitMqUtils.getConnection();
            rabbitMqUtils.creatQueue(connection,meetingId+"");

            map.put("state",0);
            map.put("meetingId",meetingId);
        }catch (Exception e){
            map.put("state",1);
            map.put("errorMessage","Start wrong");

        }

        String str = mapper.writeValueAsString(map);
        return str;
    }

    @ResponseBody
    @PostMapping("/endMeeting")
    public String stopMeeting(@RequestBody Meeting meeting) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();
        try{
            int meetingId = meeting.getMeetingId();
            System.out.println(String.valueOf(meetingId));
            meeting.setMeetingStatus(0);
            meeting.setMeetingId(meetingId);
            meetingServiceimpl.updateMeetingStatus(meeting); // update status to 0 (ended) instead of removing from db
            //meetingServiceimpl.deleteMeeting(meeting.getMeetingId()); // this will delete ended meeting from db
            map.put("state",0);
            map.put("meetingId",meeting.getMeetingId());


        }catch (Exception e){
            map.put("state",1);
            map.put("errorMessage","wrong delete");
        }

        String str = mapper.writeValueAsString(map);
        return str;
    }

    @ResponseBody
    @GetMapping("/meetingList")
    public String showMeeting() throws JsonProcessingException {
        List<Meeting> meetingList = meetingServiceimpl.queryMeetingList();

        List<Map<String,Object>> meetings = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();
        for (Meeting meeting : meetingList) {
            Map<String,Object> eachMeet = new HashMap<>();
            eachMeet.put("meetingId",meeting.getMeetingId());
            eachMeet.put("roomTitle",meeting.getRoomTitle());
            eachMeet.put("userName",meeting.getUserName());
            eachMeet.put("roomDescription", meeting.getRoomDescription());
            eachMeet.put("direct", meeting.getDirect());
            eachMeet.put("imageUrl", meeting.getImageUrl());
            eachMeet.put("status", meeting.getStatus());
            eachMeet.put("modelType", meeting.getModelType());
            if(meeting.getPwd()!=null){
                eachMeet.put("pwd",meeting.getPwd());
            }
            meetings.add(eachMeet);
        }
        map.put("state",0);
        map.put("meeting",meetings);
        String str = mapper.writeValueAsString(map);
        return str;
    }


    @ResponseBody
    @PostMapping("/joinMeeting")
    public String joinMeeting(@RequestBody Meeting meeting) throws JsonProcessingException {
        Meeting serchMeeting = meetingServiceimpl.queryMeeting(meeting.getMeetingId(),meeting.getPwd());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();

        if(serchMeeting != null) {
            map.put("state",0);
            map.put("roomTitle", serchMeeting.getRoomTitle());
            map.put("userName",serchMeeting.getUserName());
            map.put("userId",serchMeeting.getUserId());
        }else{
            map.put("state",1);
            map.put("errorMessage","wrong meetingId or meeting password");
        }
        String str = mapper.writeValueAsString(map);
        return str;
    }

    @ResponseBody
    @GetMapping("/meetingRecord")
    public String meetingRecord(@RequestParam(required = true) String meetingId) throws JsonProcessingException {
        String record = fileService.readFile(meetingId, true);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("state",0);
        map.put("record",record);
        String str = mapper.writeValueAsString(map);
        return str;
    }


}
