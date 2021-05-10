package com.lyf.service;

import com.lyf.pojo.Meeting;

import java.util.List;

public interface MeetingService {
    List<Meeting> queryMeetingList();

    Meeting queryMeetingById(int meetingId);

    Meeting queryMeeting(int meetingId,String pwd);

    int addMeeting(Meeting meeting);

    int updateMeeting(Meeting meeting);
    int updateMeetingStatus(Meeting meeting);

    int deleteMeeting(int meetingId);

    int creatNewId();
}

