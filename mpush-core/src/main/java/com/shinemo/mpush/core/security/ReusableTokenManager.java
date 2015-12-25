package com.shinemo.mpush.core.security;

import com.shinemo.mpush.api.SessionInfo;
import com.shinemo.mpush.tools.crypto.MD5Utils;

/**
 * Created by ohun on 2015/12/25.
 */
public class ReusableTokenManager {
    public static final ReusableTokenManager INSTANCE = new ReusableTokenManager();
    private static final int EXPIRE_TIME = 24 * 60 * 60 * 1000;


    public boolean saveToken(ReusableToken token) {
        return true;
    }

    public ReusableToken getToken() {
        return new ReusableToken();
    }

    public ReusableToken genToken(SessionInfo info) {
        /**
         * 先生成key，需要保证半个周期内同一个设备生成的key是相同的
         */
        long partition = System.currentTimeMillis() / (EXPIRE_TIME / 2);//把当前时间按照半个周期划分出一个当前所属于的区域
        StringBuilder sb = new StringBuilder();
        sb.append(info.deviceId).append('_').append(partition).append("_R_S_T");
        ReusableToken v = new ReusableToken();
        v.tokenId = MD5Utils.encrypt(sb.toString());
        v.clientVersion = info.clientVersion;
        v.deviceId = info.deviceId;
        v.osName = info.osName;
        v.osVersion = info.osVersion;
        v.desKey = info.desKey;
        /**
         * 计算失效时间
         */
        long nowTime = System.currentTimeMillis();
        long willExpire = (nowTime / EXPIRE_TIME + 1) * EXPIRE_TIME;//预计的到下个周期的失效时间

        //有可能到绝对周期的时间已经非常短了，如果已经非常短的话，再补充一个周期
        int exp;
        if (willExpire - nowTime > EXPIRE_TIME / 2) {
            exp = (int) (willExpire - nowTime);
        } else {
            exp = (int) (willExpire - nowTime) + EXPIRE_TIME;
        }
        v.expireTime = System.currentTimeMillis() + exp;//存储绝对过期时间
        return v;
    }
}
