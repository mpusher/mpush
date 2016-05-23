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

package com.mpush.test.gson;

import java.util.Map;

import org.junit.Test;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;

public class DnsMappingTest {
	
	@Test
	public void test(){
		
		ArrayListMultimap<String, String> mappings = ArrayListMultimap.create();
		
		String dnsString = "111.1.57.148=127.0.0.1,127.0.0.2;120.27.196.68=127.0.0.1;120.27.198.172=127.0.0.1";
        if (Strings.isNullOrEmpty(dnsString)) return;

        Map<String, String> map = Splitter.on(';').withKeyValueSeparator('=').split(dnsString);
        Splitter vsp = Splitter.on(',');
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String value = entry.getValue();
            if (Strings.isNullOrEmpty(value)) continue;
            Iterable<String> it = vsp.split(entry.getValue());
            mappings.putAll(entry.getKey(), it);
        }
        
        System.out.println(mappings);
		
	}

}
