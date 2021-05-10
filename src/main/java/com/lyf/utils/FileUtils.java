package com.lyf.utils;

import java.io.*;

public class FileUtils {

    public static String readText(String filePath) {
        String lines = "";
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                //lines += line + "\n";
                lines+=line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }


    public static void writeText(String filePath, String content,boolean isAppend) {
        FileOutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            outputStream = new FileOutputStream(filePath,isAppend);
            outputStreamWriter = new OutputStreamWriter(outputStream);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if(bufferedWriter != null){
                    bufferedWriter.close();
                }
                if (outputStreamWriter != null){
                    outputStreamWriter.close();
                }
                if (outputStream != null){
                    outputStream.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}
