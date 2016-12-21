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

package com.mpush.tools.thread;

public final class ThreadNames {
    public static final String NS = "mp";
    public static final String THREAD_NAME_PREFIX = NS + "-t";
    public static final String T_BOSS = NS + "-boss";
    public static final String T_WORKER = NS + "-work";
    public static final String T_CONN_BOSS = NS + "-conn-boss";
    public static final String T_GATEWAY_BOSS = NS + "-gateway-boss";
    public static final String T_ADMIN_BOSS = NS + "-admin-boss";
    public static final String T_CONN_WORKER = NS + "-conn-work";
    public static final String T_ADMIN_WORKER = NS + "-admin-work";
    public static final String T_GATEWAY_WORKER = NS + "-gateway-work";
    public static final String T_TRAFFIC_SHAPING = NS + "-traffic-shaping";
    public static final String T_TCP_CLIENT = NS + "-tcp-client";
    public static final String T_HTTP_CLIENT = NS + "-http-client-work";
    public static final String T_EVENT_BUS = NS + "-event";
    public static final String T_MQ = NS + "-mq";
    public static final String T_ARK_REQ_TIMER = NS + "-ack-timer";
    public static final String T_PUSH_CLIENT_TIMER = NS + "-push-client-timer";
    public static final String T_PUSH_CENTER_TIMER = NS + "-push-center-timer";
    public static final String T_CONN_TIMER = NS + "-conn-check-timer";
    public static final String T_HTTP_TIMER = NS + "-http-client-timer";
    public static final String T_HTTP_DNS_TIMER = NS + "-http-dns-timer";
    public static final String T_MONITOR = "monitor";

}
