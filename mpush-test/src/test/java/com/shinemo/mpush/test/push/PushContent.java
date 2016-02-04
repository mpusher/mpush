package com.shinemo.mpush.test.push;

public class PushContent {
    public String msgId;
    public String title;
    public String content;
    public int msgType;

    public PushContent(String msgId, String title, String content) {
        this.msgId = msgId;
        this.title = title;
        this.content = content;
    }
}