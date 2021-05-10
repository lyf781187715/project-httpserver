package com.lyf.mapper;

import com.lyf.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface UserMapper {

        List<User> queryUserList();

        User queryUserByName(String userName);

        User queryUser(String userName, String userPwd);

        int addUser(User user);

        int updateUser(User user);

        int deleteUser(String userName);
}
