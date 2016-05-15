package com.mpush.common.security;

import com.mpush.api.connection.Cipher;
import com.mpush.tools.Profiler;
import com.mpush.tools.crypto.RSAUtils;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by ohun on 2015/12/28.
 *
 * @author ohun@live.cn
 */
public final class RsaCipher implements Cipher {
    private final RSAPrivateKey privateKey;

    private final RSAPublicKey publicKey;

    public RsaCipher(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    @Override
    public byte[] decrypt(byte[] data) {
    	try{
    		Profiler.enter("start rsa decrypt");
    		return RSAUtils.decryptByPrivateKey(data, privateKey);
    	}finally{
    		Profiler.release();
    	}
        
    }

    @Override
    public byte[] encrypt(byte[] data) {
    	try{
    		Profiler.enter("start rsa encrypt");
    		return RSAUtils.encryptByPublicKey(data, publicKey);
    	}finally{
    		Profiler.release();
    	}
    }

	@Override
	public String toString() {
		return "RsaCipher [privateKey=" + new String(privateKey.getEncoded()) + ", publicKey=" + new String(publicKey.getEncoded()) + "]";
	}
    
}
