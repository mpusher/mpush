package com.shinemo.mpush.core.security;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ohun on 2015/12/25.
 */
public class CredentialManagerTest {

    @Test
    public void testGetPrivateKey() throws Exception {
        CredentialManager.INSTANCE.getPrivateKey();
    }

    @Test
    public void testGetPublicKey() throws Exception {

    }
}