package com.lyf.service;

import com.lyf.mapper.UserMapper;
import com.lyf.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceimpl implements UserService {

    @Autowired
    UserMapper userMapper;
    @Override
    public List<User> queryUserList() {
        return userMapper.queryUserList();
    }

    @Override
    public User queryUserByName(String userName) {
        return userMapper.queryUserByName(userName);
    }

    @Override
    public User queryUser(String userName,String userPwd) { return userMapper.queryUser(userName,userPwd); }

    @Override
    public int addUser(User user) {
        return userMapper.addUser(user);
    }

    @Override
    public int updateUser(User user) {
        return userMapper.updateUser(user);
    }

    @Override
    public int deleteUser(String userName) {
        return userMapper.deleteUser(userName);
    }
}
