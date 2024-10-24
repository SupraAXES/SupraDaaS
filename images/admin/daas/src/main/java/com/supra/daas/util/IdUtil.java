package com.supra.daas.util;

import java.util.UUID;

public class IdUtil {

    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    public static String generateShortKey() {
        String uuid = generateUuid();
        return uuid.substring(0, 8);
    }
    
}
