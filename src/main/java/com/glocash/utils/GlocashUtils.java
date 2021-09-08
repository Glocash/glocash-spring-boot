package com.glocash.utils;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class GlocashUtils {

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());

    //private static Random orderRandom = new Random();


    /**
     * 高并发下面存在问题
     */
    public synchronized static String getTimeStamp(long dateTime) {
        return DATE_FORMAT.format(new Date(dateTime * 1000L));
    }


    /**
     * hash sha256 加密
     * @param str
     * @return
     */
    public static String getSHA256StrJava(String str){
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (java.security.NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encodeStr;
    }

    public static String byte2Hex(byte[] bytes){
        StringBuilder stringBuffer = new StringBuilder();
        String temp = null;

        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }

            stringBuffer.append(temp);
        }

        return stringBuffer.toString();
    }



}
