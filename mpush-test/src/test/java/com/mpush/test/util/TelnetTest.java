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

package com.mpush.test.util;

import com.mpush.tools.Utils;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class TelnetTest {

    @Test
    public void test() {
        boolean ret = Utils.checkHealth("120.27.196.68", 82);
        System.out.println(ret);
    }

    @Test
    public void test2() {
        boolean ret = Utils.checkHealth("120.27.196.68", 80);
        System.out.println(ret);
    }

    @Test
    public void uriTest() throws URISyntaxException {
        String url = "http://127.0.0.1";
        URI uri = new URI(url);
        System.out.println(uri.getPort());
    }


}
