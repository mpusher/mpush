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

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.mpush.tools.config.data.DnsMapping;
import com.mpush.tools.log.Logs;
import org.aeonbits.owner.Converter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DnsMappingConverter implements Converter<Map<String, List<DnsMapping>>> {

    @Override
    public Map<String, List<DnsMapping>> convert(Method method, String input) {
        Logs.Console.info("method:" + method.getName() + ", input:" + input);

        Map<String, String> map = Splitter.on(';').withKeyValueSeparator('=').split(input);
        Map<String, List<DnsMapping>> result = Maps.newConcurrentMap();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            List<DnsMapping> dnsMappings = Lists.transform(Arrays.asList(value.split(",")), new Function<String, DnsMapping>() {
                @Override
                public DnsMapping apply(String ipAndPort) {
                    if (ipAndPort.contains(":")) {
                        String[] temp = ipAndPort.split(":");
                        return new DnsMapping(temp[0], Ints.tryParse(temp[1]));
                    } else {
                        return new DnsMapping(ipAndPort, 80);
                    }
                }
            });
            result.put(key, dnsMappings);
        }
        return result;

    }

}
