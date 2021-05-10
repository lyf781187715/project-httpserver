package com.lyf.pojo;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Meeting {
    int meetingId;
    String pwd;
    String roomTitle;
    String userName;
    String roomDescription;
    String imageUrl;
    int userId;
    int direct;
    int status;

    public Meeting(int meetingId, String pwd, String roomTitle, String userName, String roomDescription, int userId, int direct, String imageUrl, int status) {
        this.meetingId = meetingId;
        this.pwd = pwd;
        this.roomTitle = roomTitle;
        this.userName = userName;
        this.roomDescription = roomDescription;
        this.userId = userId;
        this.direct = direct;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public void setMeetingStatus(int i) {
        meetingId = i;
    }
}