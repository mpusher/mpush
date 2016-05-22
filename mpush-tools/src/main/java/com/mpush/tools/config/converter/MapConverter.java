package com.mpush.tools.config.converter;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.mpush.tools.log.Logs;
import org.aeonbits.owner.Converter;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * Created by yxx on 2016/5/12.
 *
 * @author ohun@live.cn
 */
public class MapConverter implements Converter<Map<String, String>> {

    @Override
    public Map<String, String> convert(Method method, String input) {
        Logs.Console.info("method:" + method.getName() + ", input:" + input);
        if (Strings.isNullOrEmpty(input)) return Collections.emptyMap();
        return Splitter.on(',').withKeyValueSeparator(':').split(input);
    }
}
