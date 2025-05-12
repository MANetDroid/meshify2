package com.manetdroid.meshify2.framework.exceptions;

public class MessageException extends Exception {
    public MessageException(String str) {
        super(str);
    }

    public MessageException(String str, Exception e) {
        super(str, e);
    }

    public MessageException(Exception e) {
        super(e);
    }
}