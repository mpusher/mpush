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
        Logs.Console.error("method:" + method.getName() + ", input:" + input);

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
