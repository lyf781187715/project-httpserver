package com.lyf.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User {
    private int userId;
    private String userName;
    private String userPwd;

    public User(int userId, String userName, String userPwd) {
        this.userId = userId;
        this.userName = userName;
        this.userPwd = userPwd;
    }

    public User(String userName, String userPwd) {
        this.userName = userName;
        this.userPwd = userPwd;
    }
}
