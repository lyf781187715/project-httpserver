package com.lyf;

import com.lyf.pojo.Translate;
import com.lyf.pojo.User;
import com.lyf.service.MeetingServiceimpl;
import com.lyf.service.TranslateService;
import com.lyf.service.UserServiceimpl;
import com.lyf.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.FileNotFoundException;

@SpringBootTest
class ProjectHttpserverApplicationTests {

    @Autowired
    MeetingServiceimpl meetingServiceimpl;
    @Test
    void contextLoads() {
        System.out.println(meetingServiceimpl.queryMeetingList().toString());
    }


    @Autowired
    UserServiceimpl userServiceimpl;
    @Test
    void test(){
        System.out.println(userServiceimpl.queryUserList().toString());
    }

    @Autowired
    TranslateService translateService;
    @Test
    void testforhttp(){
        Translate translate = new Translate("1",0,1,"123","456","12");
        translateService.sendPost(translate);
    }

    @Value(value = "${filepath}")
    private String filepath;
    @Test
    void testforfile() throws FileNotFoundException {
        System.out.println(filepath);
        FileUtils.writeText(filepath,"1231222",true);
        System.out.println(FileUtils.readText(filepath));
    }
}
