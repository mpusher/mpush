/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.api.srd;

/**
 * Created by ohun on 2016/12/27.
 *
 * 服务名称
 *
 * @author ohun@live.cn (夜色)
 */
public interface ServiceNames {
    String CONN_SERVER = "/cluster/cs";
    String WS_SERVER = "/cluster/ws";
    String GATEWAY_SERVER = "/cluster/gs";
    String DNS_MAPPING = "/dns/mapping";

    String ATTR_PUBLIC_IP = "public_ip";

}
