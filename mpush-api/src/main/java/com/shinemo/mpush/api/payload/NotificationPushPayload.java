package com.shinemo.mpush.api.payload;

import java.util.HashMap;
import java.util.Map;



/**
 * msgId、msgType 必填 
 * msgType=1 :nofication,提醒。
 *            必填:title，content。没有title，则为应用名称。 
 *            非必填。nid 通知id,主要用于聚合通知。
 * 			  content 为push  message。附加的一些业务属性，都在里边。json格式
 * msgType=2 :非通知消息。不在通知栏展示。
 *            必填：content。
 * msgType=3 :消息+提醒
 *            作为一个push消息过去。和jpush不一样。jpush的消息和提醒是分开发送的。
 *        
 *
 */
public class NotificationPushPayload implements Payload{
    
	private static final long serialVersionUID = 4363667286689742483L;
	private String title;
	private String content;
	
	private Map<String,String> extras;
	
	public String getTitle() {
		return title;
	}
	public NotificationPushPayload setTitle(String title) {
		this.title = title;
		return this;
	}
	public String getContent() {
		return content;
	}
	public NotificationPushPayload setContent(String content) {
		this.content = content;
		return this;
	}
	
	/*****以下非必填**/
    private Long nid; //主要用于聚合通知，非必填
    private byte flags; //特性字段。 0x01:声音   0x02:震动 0x03:闪灯
    private String largeIcon; // 大图标
    private String ticker; //和title一样
    private Integer number; //
	public Long getNid() {
		return nid;
	}
	public NotificationPushPayload  setNid(Long nid) {
		this.nid = nid;
		return this;
	}
	public Byte getFlags() {
		return flags;
	}
	public NotificationPushPayload setFlags(Byte flags) {
		this.flags = flags;
		return this;
	}
	public String getLargeIcon() {
		return largeIcon;
	}
	public NotificationPushPayload setLargeIcon(String largeIcon) {
		this.largeIcon = largeIcon;
		return this;
	}
	public String getTicker() {
		return ticker;
	}
	public NotificationPushPayload setTicker(String ticker) {
		this.ticker = ticker;
		return this;
	}
	public Integer getNumber() {
		return number;
	}
	public NotificationPushPayload setNumber(Integer number) {
		this.number = number;
		return this;
	}
    
	public NotificationPushPayload setFlag(Flag flag) {
        this.flags |= flag.getValue();
        return this;
    }

    public boolean hasFlag(Flag flag) {
        return (this.flags & flag.getValue()) != 0;
    }
    
    public Map<String, String> getExtras() {
		return extras;
	}
    
	public NotificationPushPayload setExtras(Map<String, String> extras) {
		this.extras = new HashMap<>(extras);
		return this;
	}

	public enum Flag {
        VOICE("声音",0x01),
        SHOCK("震动",0x02),
        LIGHT("闪灯",0x03);

        Flag(String desc, int value) {
        	this.desc = desc;
        	this.value = value;
        }

        private final String desc;
        private final Integer value;
        
		public String getDesc() {
			return desc;
		}
		public byte getValue() {
			return value.byteValue();
		}

    }
	
	
}

