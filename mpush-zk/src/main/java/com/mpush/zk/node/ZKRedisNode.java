package com.mpush.zk.node;

import com.mpush.tools.config.data.RedisGroup;
import com.mpush.tools.Jsons;

/**
 * Created by yxx on 2016/5/18.
 *
 * @author ohun@live.cn
 */
public class ZKRedisNode extends RedisGroup implements ZKNode {
    private transient String zkPath;

    public String getZkPath() {
        return zkPath;
    }

    public void setZkPath(String zkPath) {
        this.zkPath = zkPath;
    }

    @Override
    public String encode() {
        return Jsons.toJson(this);
    }
}
