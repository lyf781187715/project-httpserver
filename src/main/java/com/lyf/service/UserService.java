package com.lyf.service;

import com.lyf.pojo.User;

import java.util.List;

public interface UserService {


    List<User> queryUserList();

    User queryUserByName(String userName);

    User queryUser(String userName,String userPwd);

    int addUser(User user);

    int updateUser(User user);

    int deleteUser(String userName);
}
