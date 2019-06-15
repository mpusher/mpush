package com.mpush.api.push;

import java.util.Map;

/**
 * @description:
 * @author: dengliaoyan
 * @create: 2019-06-15 13:48
 **/
public class Notification {
    /**
     * {
     *     "userId":"",
     *     "alias":"",
     *     "tags":"",
     *     "title":"",
     *     "content":"",
     *     "flags":"",
     *     "msgType":"",
     *     "extras":{
     *         "expire":""
     *     }
     * }
     */
    public String msgId; // 推送消息id
    public String title; // 标题 必填
    public String content; // 内容 必填
    public Byte flags; //特性字段。 0x01:声音  0x02:震动  0x03:闪灯
    public String largeIcon; // 大图标
    public String ticker; //和title一样
    public Map<String, String> extras; // 扩展
}
