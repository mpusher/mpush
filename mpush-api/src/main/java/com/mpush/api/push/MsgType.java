package com.mpush.api.push;

public enum MsgType {
    NOTIFICATION("提醒", 1),//会在通知栏显示
    MESSAGE("消息", 2),//不会在通知栏显示,业务自定义消息
    NOTIFICATION_AND_MESSAGE("提醒+消息", 3);//1+2

    MsgType(String desc, int value) {
        this.desc = desc;
        this.value = value;
    }

    private final String desc;
    private final int value;

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }
}