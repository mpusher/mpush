package com.shinemo.mpush.api.protocol;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by ohun on 2015/12/19.
 * magic(2)+cmd(1)+version(1)+flags(1)+msgId(4)+length(4)+body(n)
 */
public class Packet implements Serializable{
	private static final long serialVersionUID = -2725825199998223372L;
	public byte command;
    public byte version;
    public byte flags;
    public int msgId;
    public byte[] body;

    public int getBodyLength() {
        return body == null ? 0 : body.length;
    }

	@Override
	public String toString() {
		return "Packet [command=" + command + ", version=" + version + ", flags=" + flags + ", msgId=" + msgId + ", body=" + Arrays.toString(body) + "]";
	}
    
    
}
