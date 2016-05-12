package com.shinemo.mpush.tools.config;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.aeonbits.owner.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * Created by yxx on 2016/5/12.
 *
 * @author ohun@live.cn
 */
public class MapConverter implements Converter<Map<String, String>> {
    private static final Logger log = LoggerFactory.getLogger(DnsMappingConverter.class);

    @Override
    public Map<String, String> convert(Method method, String input) {
        log.warn("method:" + method.getName() + "," + input);
        if (Strings.isNullOrEmpty(input)) return Collections.emptyMap();
        return Splitter.on(',').withKeyValueSeparator(':').split(input);
    }
}
