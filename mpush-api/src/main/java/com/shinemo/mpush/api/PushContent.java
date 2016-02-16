package com.shinemo.mpush.api;

public final class PushContent {
    public String msgId; //返回使用
    public String title;
    public String content; //content
    public int msgType; //type
    
    public PushContent(int msgType) {
    	this.msgType = msgType;
	}

    public static PushContent build(int msgType){
    	PushContent pushContent = new PushContent(msgType);
    	return pushContent;
    }

	public String getMsgId() {
		return msgId;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public int getMsgType() {
		return msgType;
	}

	public PushContent setTitle(String title) {
		this.title = title;
		return this;
	}

	public PushContent setContent(String content) {
		this.content = content;
		return this;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	
    
}