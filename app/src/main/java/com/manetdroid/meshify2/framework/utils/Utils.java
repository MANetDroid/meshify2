package com.manetdroid.meshify2.framework.utils;

import java.util.UUID;

public class Utils {

    public static String generateSessionId() {
        return UUID.randomUUID().toString().substring(0, 5);
    }
}
