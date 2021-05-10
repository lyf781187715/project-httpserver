package com.lyf.service;

import com.lyf.mapper.MeetingMapper;
import com.lyf.pojo.Meeting;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class MeetingServiceimpl implements MeetingService{

    @Autowired
    MeetingMapper meetingMapper;
    @Override
    public List<Meeting> queryMeetingList() {
        return meetingMapper.queryMeetingList();
    }

    @Override
    public Meeting queryMeetingById(@Param("meetingId") int meetingId) {
        return meetingMapper.queryMeetingById(meetingId);
    }

    @Override
    public Meeting queryMeeting(@Param("meetingId")int meetingId,
                                @Param("pwd")String pwd) { return meetingMapper.queryMeeting(meetingId,pwd); }

    @Override
    public int addMeeting(@Param("meeting") Meeting meeting) {
        return meetingMapper.addMeeting(meeting);
    }

    @Override
    public int updateMeeting(@Param("meeting") Meeting meeting) {
        return meetingMapper.updateMeeting(meeting);
    }

    @Override
    public int updateMeetingStatus(@Param("meeting") Meeting meeting) {
        return meetingMapper.updateMeetingStatus(meeting);
    }

    @Override
    public int deleteMeeting(@Param("meetingId")int meetingId) {
        return meetingMapper.deleteMeeting(meetingId);
    }

    @Override
    public int creatNewId() {
        long time = System.currentTimeMillis();
        Random random = new Random();
        int i = random.nextInt(999);
        return Math.abs((int) (time*1000+i));
    }
}

