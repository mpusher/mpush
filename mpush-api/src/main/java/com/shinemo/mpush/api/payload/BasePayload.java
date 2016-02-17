package com.shinemo.mpush.api.payload;


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
@SuppressWarnings("unchecked")
public class BasePayload<T> implements Payload{
    
	private static final long serialVersionUID = 2367820805720853376L;
	/*****以下非必填**/
    private Long nid; //主要用于聚合通知，非必填
    private Byte flags; //特性字段。 0x01:声音   0x02:震动 0x03:闪灯
    private String largeIcon; // 大图标
    private String ticker; //和title一样
    private Integer number; //
	public Long getNid() {
		return nid;
	}
	public T  setNid(Long nid) {
		this.nid = nid;
		return (T)this;
	}
	public Byte getFlags() {
		return flags;
	}
	public T setFlags(Byte flags) {
		this.flags = flags;
		return (T)this;
	}
	public String getLargeIcon() {
		return largeIcon;
	}
	public T setLargeIcon(String largeIcon) {
		this.largeIcon = largeIcon;
		return (T)this;
	}
	public String getTicker() {
		return ticker;
	}
	public T setTicker(String ticker) {
		this.ticker = ticker;
		return (T)this;
	}
	public Integer getNumber() {
		return number;
	}
	public T setNumber(Integer number) {
		this.number = number;
		return (T)this;
	}
    
}

