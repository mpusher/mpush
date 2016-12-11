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

package com.mpush.test.connection.body;

import com.mpush.common.message.HandshakeMessage;
import com.mpush.tools.crypto.RSAUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;

import java.security.interfaces.RSAPrivateKey;

public class BodyTest {

    private String daily_publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";
    private String daily_allocServer = "http://111.1.57.148/mpns/push/server/";
    private String daily_privateKey = "MIIBNgIBADANBgkqhkiG9w0BAQEFAASCASAwggEcAgEAAoGBAKCE8JYKhsbydMPbiO7BJVq1pbuJWJHFxOR7L8Hv3ZVkSG4eNC8DdwAmDHYu/wadfw0ihKFm2gKDcLHp5yz5UQ8PZ8FyDYvgkrvGV0ak4nc40QDJWws621dm01e/INlGKOIStAAsxOityCLv0zm5Vf3+My/YaBvZcB5mGUsPbx8fAgEAAoGAAy0+WanRqwRHXUzt89OsupPXuNNqBlCEqgTqGAt4Nimq6Ur9u2R1KXKXUotxjp71Ubw6JbuUWvJg+5Rmd9RjT0HOUEQF3rvzEepKtaraPhV5ejEIrB+nJWNfGye4yzLdfEXJBGUQzrG+wNe13izfRNXI4dN/6Q5npzqaqv0E1CkCAQACAQACAQACAQACAQA=";
    private RSAPrivateKey dailyPrivateKey = null;

    private String pre_publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUzaTfOZcywslS08R1w0pLpSqM30lsM/+XiS9tI7Es7c5wOliVjeaXLdIU48bZxFRz+FyTmYCblekZA/LGlLgedtQf/kA1vHGBnrO/YHd0Re4JqHwmhdKjF/pCSVGqFRTKytDZ9/87tVqtRiPjE95r1Qdt44JzvNLcwVwCEFXMQQIDAQAB";
    private String pre_allocServer = "http://115.29.230.2/mpns/push/server/";
    private String pre_privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJTNpN85lzLCyVLTxHXDSkulKozfSWwz/5eJL20jsSztznA6WJWN5pct0hTjxtnEVHP4XJOZgJuV6RkD8saUuB521B/+QDW8cYGes79gd3RF7gmofCaF0qMX+kJJUaoVFMrK0Nn3/zu1Wq1GI+MT3mvVB23jgnO80tzBXAIQVcxBAgMBAAECgYAuChZp7pKmZPgPRXAyk3LIRtkMbwVzkf8RrqNa3EE0Snul45eG5jmjKs0mI3dh50nN+9kA3eyZtt9BGyShZmA0q6v5s24+6BMIz1Hmkkpjyq5bwwmAHu6DjB2lphYhn9OiFuimXkVKRA8KbEo3SijLmSLY/7kHNxp5F49b9KynAQJBANH+DUPJeBnyUIECcc0+je0tsH7jm6U7sj6x/BIhcvAe9RoqMe23TEysdwqys800VYuvzXoeQYnZZ1WyQA/WOVECQQC1Z6YiaEShoF9IGqYJe4JN3dj/6r4nuWn93hZRtDPJX8+sczsPmboJvE7cE4yfILDRsC16UkTobUq4z0XyZqfxAkA+M+gP/VzTKsEIBgZZyr7V0+PlIlzXjCBXi/dkE35tfG4UKw2RIeu7BpdYlujFz0vLze6qzs2RHNIMQ3nQdx+RAkBySi0nfF3RHsMpIHD/hGsiN+VhxzmquWyH34ZcT5ZZBx5GXgsV1xqYy6U7jq2IDAaa9c6RRIfZkBIFwcEl6XthAkEArpGszIis1caRAd47YXzfg1aBGG8eQ1wY2EEX1q1iJuz/KMLccCYmFl+4R5Vfsmzy65YqO+EOKw4jBhLBvtkCmA==";
    private RSAPrivateKey prePrivateKey = null;

    //MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCcNLVG4noMfOIvKfV0eJAcADO4nr0hqoj42swL8DWY8CujpUGutw7Qk5LEn6i037wlF5CwIzJ7ix2xK+IcxEonOANtlS1NKbUXOCgUtA5mdZTnvAUByN0tzGp4BGywYNiXFQmLMXG5uxN0ZfcaoRKVqLzbcMnLB7VzS4L3OxzxqwIDAQAB
    private String online_publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCcNLVG4noMfOIvKfV0eJAcADO4nr0hqoj42swL8DWY8CujpUGutw7Qk5LEn6i037wlF5CwIzJ7ix2xK+IcxEonOANtlS1NKbUXOCgUtA5mdZTnvAUByN0tzGp4BGywYNiXFQmLMXG5uxN0ZfcaoRKVqLzbcMnLB7VzS4L3OxzxqwIDAQAB";
    private String online_allocServer = "http://allot.mangguoyisheng.com/";
    private String online_privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJw0tUbiegx84i8p9XR4kBwAM7ievSGqiPjazAvwNZjwK6OlQa63DtCTksSfqLTfvCUXkLAjMnuLHbEr4hzESic4A22VLU0ptRc4KBS0DmZ1lOe8BQHI3S3MangEbLBg2JcVCYsxcbm7E3Rl9xqhEpWovNtwycsHtXNLgvc7HPGrAgMBAAECgYBLDaRAH9dmoqaG5NE0Gi2b1pkDTvou7+KKN46s+ci26Eb/hQqvKPOsUgvF/7Su24VqCQ2TJAZAiBJHK3+kNMgvmbZt3BA0jn2F13djixHip7gHSMUMD4a+jQ5MDtiE7TFVDNrkYvfbhgmT4g5wdWI1xoaHftDQAPA01B4nUIe04QJBAMiRwmnZsxNWKGSriMCbEI2j2t4T25SLcCpVoC5MZ+E+1P997qGo//6iDL65yvxN7PNI+5qFZ8poT5wrsS2j8JsCQQDHYD3kbQTRdCQTmK8Vl6EcL7kZUcxGlq5XsdyRG3r2bIvVE4pNnyEdpG+6qsqPUw00JTOvZ3HmST9CNuWtF+wxAkB+9rYI54RSg0HCqEtDEWXjI4xS9GMVn0b7pYRmintfvLR8ny1GLIMQn2hN+7KhEHskbljHMhfHq0xp4cagy5xtAkEAp4GHXmPtmWAfc0tjRhvXowvBUrFzk5bDMTBgpJVW8LRvovxAxUg9lV7y8/zMJDBdtoLO8r5RZm4BtMrUmmGv8QJBAIbo67g2H84eZSSFTwQ1YnjdhLBBv4TBPuhtSHH00mUWOKR9qQSp+MixRUTE1HP1htn+DQ0KI5zbY4FLAT3Nb0g=";
    private RSAPrivateKey onlinePrivateKey = null;

    String rawData = "29, -114, -24, 82, -96, -116, -19, 64, -91, 88, -119, -88, 29, 33, 9, 9, 84, 120, -62, 54, -72, 47, -77, -126, -73, 2, -38, -62, -75, 39, -114, 92, -115, -9, -81, 43, -82, 73, 2, -101, 32, 67, 112, 46, 119, -16, -6, -85, -121, -44, 25, 28, -116, 6, 56, -2, -6, -109, -75, 91, -104, 9, -84, -28, 72, 24, 94, -54, 127, -124, -125, -93, 102, 38, 11, -55, 70, -86, 101, -76, -54, -11, 80, 104, -80, 44, 53, -117, 117, -88, -96, -19, 116, 0, -62, 103, -15, -106, -5, -55, -103, -86, 72, -18, -83, -117, 39, 80, -121, -31, -105, -28, 9, 23, -24, 106, -116, 127, -77, -122, 71, -112, -79, -106, 80, 9, -86, -22";

    byte[] data = null;

    @Before
    public void init() {
        try {
            dailyPrivateKey = (RSAPrivateKey) RSAUtils.decodePrivateKey(daily_privateKey);
            prePrivateKey = (RSAPrivateKey) RSAUtils.decodePrivateKey(pre_privateKey);
            onlinePrivateKey = (RSAPrivateKey) RSAUtils.decodePrivateKey(online_privateKey);
            String[] temp = rawData.split(",");
            data = new byte[temp.length];
            for (int i = 0; i < temp.length; i++) {
                data[i] = Byte.parseByte(temp[i].trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void dailyDecode() {
        byte[] message = RSAUtils.decryptByPrivateKey(data, dailyPrivateKey);
        decode(message);
    }

    @Test
    public void preDecode() {
        byte[] message = RSAUtils.decryptByPrivateKey(data, prePrivateKey);
        decode(message);
    }

    @Test
    public void onlineDecode() {
        byte[] message = RSAUtils.decryptByPrivateKey(data, onlinePrivateKey);
        decode(message);
    }

    private void decode(byte[] message) {
        HandshakeMessage handshakeMessage = new HandshakeMessage(null);
        handshakeMessage.decode(message);
        System.out.println(ToStringBuilder.reflectionToString(handshakeMessage, ToStringStyle.MULTI_LINE_STYLE));
    }

}
