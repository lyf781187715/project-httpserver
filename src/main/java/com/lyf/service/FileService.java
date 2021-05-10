package com.lyf.service;

import com.lyf.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileService {


    @Value(value = "${filepath}")
    private String filepath;

    public void writeFile(String meetingId,String newMessage,boolean isSrc,boolean isAppend){
        String url=null;
        if(isSrc){
            url = filepath+"/"+meetingId+"_src";
        }else{
            url = filepath+"/"+meetingId+"_is";
        }

        FileUtils.writeText(url,newMessage,isAppend);
    }
    public String readFile(String meetingId,boolean isSrc){
        String url=null;
        if(isSrc){
            url = filepath+"/"+meetingId+"_src";
        }else{
            url = filepath+"/"+meetingId+"_his";
        }
        return FileUtils.readText(url);
    }
}
