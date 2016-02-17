//package com.shinemo.mpush.api;
//
//import java.io.Serializable;
//
///**
// * msgId、msgType 必填 
// * msgType=1 :nofication,提醒。
// *            必填:title，content。没有title，则为应用名称。 
// *            非必填。nid 通知id,主要用于聚合通知。
// * 			  content 为push  message。附加的一些业务属性，都在里边。json格式
// * msgType=2 :非通知消息。不在通知栏展示。
// *            必填：content。
// * msgType=3 :消息+提醒
// *            作为一个push消息过去。和jpush不一样。jpush的消息和提醒是分开发送的。
// *        
// *
// */
//public final class PushContent implements Serializable{
//	private static final long serialVersionUID = -1805329333995385960L;
//	private String msgId; //返回使用
//    private String title; //没有title，则为应用名称。
//    private String content; //content
//    private int msgType; //type
//    
//   
//    
//    public PushContent(int msgType) {
//    	this.msgType = msgType;
//	}
//
//    public static PushContent build(int msgType){
//    	PushContent pushContent = new PushContent(msgType);
//    	return pushContent;
//    }
//
//	public String getMsgId() {
//		return msgId;
//	}
//
//	public String getTitle() {
//		return title;
//	}
//
//	public String getContent() {
//		return content;
//	}
//
//	public int getMsgType() {
//		return msgType;
//	}
//
//	public PushContent setTitle(String title) {
//		this.title = title;
//		return this;
//	}
//
//	public PushContent setContent(String content) {
//		this.content = content;
//		return this;
//	}
//
//	public void setMsgId(String msgId) {
//		this.msgId = msgId;
//	}
//	
//    
//}