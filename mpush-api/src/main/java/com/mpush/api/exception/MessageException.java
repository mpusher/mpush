package com.mpush.api.exception;

public class MessageException extends RuntimeException {
	
	private static final long serialVersionUID = 8731698346169093329L;

    public MessageException(String message) {
        super(message);
    }
	
}
