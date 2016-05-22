package com.mpush.tools.config.converter;

import com.mpush.tools.config.data.RedisGroup;
import com.mpush.tools.config.data.RedisServer;
import com.mpush.tools.log.Logs;
import org.aeonbits.owner.Converter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RedisGroupConverter implements Converter<RedisGroup> {

    @Override
    public RedisGroup convert(Method method, String input) {
        Logs.Console.info("method:" + method.getName() + ", input:" + input);

        List<RedisServer> servers = new ArrayList<>();
        String[] chunks = input.split(",");
        for (String chunk : chunks) {
            String[] entry = chunk.split(":");
            String ip = entry[0].trim();
            String port = entry[1].trim();
            // 如果配置了redis密码（redis_group = 111.1.57.148:6379:ShineMoIpo）才设置密码
            // 否则密码为空，JedisPool可以兼容两种情况
            String password = null;
            if (entry.length >= 3) {
                password = entry[2].trim();
            }
            servers.add(new RedisServer(ip, Integer.parseInt(port), password));
        }

        return new RedisGroup(servers);
    }
}
