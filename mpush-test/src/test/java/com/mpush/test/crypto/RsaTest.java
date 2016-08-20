/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.test.crypto;

import com.mpush.common.message.HandshakeMessage;
import com.mpush.common.security.CipherBox;
import com.mpush.tools.crypto.RSAUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RsaTest {

    private String daily_publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";
    private String daily_privateKey = "MIIBNgIBADANBgkqhkiG9w0BAQEFAASCASAwggEcAgEAAoGBAKCE8JYKhsbydMPbiO7BJVq1pbuJWJHFxOR7L8Hv3ZVkSG4eNC8DdwAmDHYu/wadfw0ihKFm2gKDcLHp5yz5UQ8PZ8FyDYvgkrvGV0ak4nc40QDJWws621dm01e/INlGKOIStAAsxOityCLv0zm5Vf3+My/YaBvZcB5mGUsPbx8fAgEAAoGAAy0+WanRqwRHXUzt89OsupPXuNNqBlCEqgTqGAt4Nimq6Ur9u2R1KXKXUotxjp71Ubw6JbuUWvJg+5Rmd9RjT0HOUEQF3rvzEepKtaraPhV5ejEIrB+nJWNfGye4yzLdfEXJBGUQzrG+wNe13izfRNXI4dN/6Q5npzqaqv0E1CkCAQACAQACAQACAQACAQA=";
    private static RSAPrivateKey dailyPrivateKey = null;
    private static RSAPublicKey dailyPublicKey = null;

    @Before
    public void init() {
        try {
            dailyPrivateKey = (RSAPrivateKey) RSAUtils.decodePrivateKey(daily_privateKey);
            dailyPublicKey = (RSAPublicKey) RSAUtils.decodePublicKey(daily_publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {

        HandshakeMessage message = new HandshakeMessage(null);
        message.clientKey = CipherBox.I.randomAESKey();
        message.iv = CipherBox.I.randomAESIV();
        message.clientVersion = "1.1.0";
        message.deviceId = "dscsdcsdcsdcdscsdcdscsdcdscdscds";
        message.osName = "android";
        message.osVersion = "1.2.1";
        message.timestamp = System.currentTimeMillis();

        byte[] temp = message.encode();

        long startencode = System.currentTimeMillis();
        byte[] encode = RSAUtils.encryptByPublicKey(temp, dailyPublicKey);
        long encodeTime = System.currentTimeMillis() - startencode;

        long startdecode = System.currentTimeMillis();
        byte[] temp2 = RSAUtils.decryptByPrivateKey(encode, dailyPrivateKey);
        long decodeTime = System.currentTimeMillis() - startdecode;
        decode(temp2);
        System.out.println(encodeTime + "," + decodeTime);

    }

    @Test
    public void mulTest() {

        Executor pool = Executors.newFixedThreadPool(20);

        CountDownLatch encodeLatch = new CountDownLatch(1);
        CountDownLatch decodeLatch = new CountDownLatch(1);

        for (int i = 0; i < 18; i++) {
            pool.execute(new Worker(i, encodeLatch, decodeLatch));
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        encodeLatch.countDown();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        decodeLatch.countDown();


        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("end");

    }

    private static void decode(byte[] message) {
        HandshakeMessage handshakeMessage = new HandshakeMessage(null);
        handshakeMessage.decode(message);
//		System.out.println(ToStringBuilder.reflectionToString(handshakeMessage, ToStringStyle.MULTI_LINE_STYLE));
    }

    public static class Worker implements Runnable {

        private int i;
        private CountDownLatch encodeLatch;
        private CountDownLatch decodeLatch;

        public Worker(int i, CountDownLatch encodeLatch, CountDownLatch decodeLatch) {
            this.i = i;
            this.encodeLatch = encodeLatch;
            this.decodeLatch = decodeLatch;
        }

        @Override
        public void run() {
            HandshakeMessage message = new HandshakeMessage(null);
            message.clientKey = CipherBox.I.randomAESKey();
            message.iv = CipherBox.I.randomAESIV();
            message.clientVersion = "1.1.0" + i;
            message.deviceId = "dscsdcsdcsdcdscsdcdscsdcdscdscds";
            message.osName = "android";
            message.osVersion = "1.2.1";
            message.timestamp = System.currentTimeMillis();

            byte[] temp = message.encode();
            System.out.println(i + ":wait encode");
            try {
                encodeLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long startencode = System.currentTimeMillis();
            byte[] encode = RSAUtils.encryptByPublicKey(temp, dailyPublicKey);
            long encodeTime = System.currentTimeMillis() - startencode;


            System.out.println(i + ":wait decode");

            try {
                decodeLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long startdecode = System.currentTimeMillis();
            byte[] temp2 = RSAUtils.decryptByPrivateKey(encode, dailyPrivateKey);
            long decodeTime = System.currentTimeMillis() - startdecode;
            decode(temp2);
            System.out.println(i + ":" + encodeTime + "," + decodeTime);

        }

    }

}
