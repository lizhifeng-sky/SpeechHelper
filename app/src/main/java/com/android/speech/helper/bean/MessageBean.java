package com.android.speech.helper.bean;

/**
 * @author lizhifeng
 * @date 2020/12/24 11:21
 */
public class MessageBean {
    public static int USER = 1;
    public static int AI = 2;
    private String message;
    private int sendType;

    public MessageBean(int sendType, String message) {
        this.message = message;
        this.sendType = sendType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSendType() {
        return sendType;
    }

    public void setSendType(int sendType) {
        this.sendType = sendType;
    }
}
