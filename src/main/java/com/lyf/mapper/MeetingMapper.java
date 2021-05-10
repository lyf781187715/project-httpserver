package com.lyf.mapper;

import com.lyf.pojo.Meeting;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface MeetingMapper {
    List<Meeting> queryMeetingList();

    Meeting queryMeetingById(int meetingId);

    Meeting queryMeeting(int meetingId,String pwd);

    int addMeeting(Meeting meeting);

    int updateMeeting(Meeting meeting);
    int updateMeetingStatus(Meeting meeting);

    int deleteMeeting(int meetingId);


}


