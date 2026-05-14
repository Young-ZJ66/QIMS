package com.young.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 加密工具类
 */
public class MD5Utils {

    public static String encrypt(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(source.getBytes("UTF-8"));
            byte[] byteDigest = md.digest();
            StringBuilder buf = new StringBuilder("");
            for (int offset = 0; offset < byteDigest.length; offset++) {
                int i = byteDigest[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            // 32位小写
            return buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
