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

import com.mpush.api.spi.net.DnsMapping;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class DnsMappingTest {

    @Test
    public void test() throws MalformedURLException {
        String url = "http://baidu.com:9001/xxx/xxx?s=nc=1";
        DnsMapping mapping = new DnsMapping("127.0.0.1", 8080);
        String s = mapping.translate(new URL(url));
        System.out.println(url);
        System.out.println(s);

    }

}
