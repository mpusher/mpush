package com.shinemo.mpush.core.security;

import com.shinemo.mpush.tools.crypto.RSAUtils;

import java.security.interfaces.RSAPrivateKey;

/**
 * Created by ohun on 2015/12/24.
 */
public class CredentialManager {
    public static final CredentialManager INSTANCE = new CredentialManager();

    public RSAPrivateKey getPrivateKey() {
        return RSAUtils.getPrivateKey("", "");
    }

}
