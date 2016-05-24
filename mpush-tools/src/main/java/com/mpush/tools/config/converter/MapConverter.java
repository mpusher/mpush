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
        Logs.Console.error("method:" + method.getName() + ", input:" + input);
        if (Strings.isNullOrEmpty(input)) return Collections.emptyMap();
        return Splitter.on(',').withKeyValueSeparator(':').split(input);
    }
}
