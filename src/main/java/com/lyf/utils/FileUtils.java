package com.lyf.utils;

import java.io.*;
import org.apache.commons.io.input.ReversedLinesFileReader;

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

    public static String readLastText(String filePath){
        File file = new File(filePath);
        String line = reverseLines(file);
        return line;
    }

    public static String reverseLines(File file){
        ReversedLinesFileReader object = null;
        String line="";
        try {
            object = new ReversedLinesFileReader(file);
            line =  object.readLine();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                object.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return line;
    }

    public static void removeText(String filePath){
        try {
            RandomAccessFile f = new RandomAccessFile(filePath, "rw");
            long length = f.length() - 1;
            byte b;
            do {
                length -= 1;
                f.seek(length);
                b = f.readByte();
            } while (b != 10);
            f.setLength(length + 1);
            f.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void writeText(String filePath, String content,boolean isAppend) {
        FileOutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            outputStream = new FileOutputStream(filePath,isAppend);
            outputStreamWriter = new OutputStreamWriter(outputStream);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.newLine(); // add a new line before writing content
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