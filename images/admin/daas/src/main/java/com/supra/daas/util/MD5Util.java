package com.supra.daas.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    public static String md5(String content) {
        if (content == null || content.length() == 0) {
            throw new IllegalArgumentException("String to encript cannot be null or zero length");
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(content.getBytes());
            byte[] hash = md.digest();
            return hex2String(hash);
        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
        }

        return null;
    }

    public static String hmac(String key, String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            byte[] secretByte = key.getBytes("UTF-8");
            byte[] dataBytes = content.getBytes("UTF-8");
            SecretKey secret = new SecretKeySpec(secretByte, "HMACSHA256");
            mac.init(secret);

            byte[] hash = mac.doFinal(dataBytes);
            return hex2String(hash);
        } catch (Exception e) {
//            e.printStackTrace();
        }

        return null;
    }

    private static String hex2String(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            if ((0xff & hash[i]) < 0x10) {
                hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
            } else {
                hexString.append(Integer.toHexString(0xFF & hash[i]));
            }
        }

        return hexString.toString();
    }
}
