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
    public static final String THREAD_NAME_PREFIX = NS + "-t-";

    /**
     * netty boss 线程
     */
    public static final String T_SERVER_BOSS = NS + "-boss-";

    /**
     * netty worker 线程
     */
    public static final String T_SERVER_WORKER = NS + "-worker-";

    public static final String T_HTTP_CLIENT = NS + "-http-";

    public static final String T_EVENT_BUS = NS + "-event-";

    public static final String T_MQ = NS + "-mq-";

    public static final String T_ZK = NS + "-zk-";

    public static final String T_BIZ = NS + "-biz-";
    public static final String T_PUSH_CALLBACK = NS + "-push-cb-";
    public static final String T_PUSH_REQ_TIMER = NS + "-push-timer-";
    public static final String T_ARK_REQ_TIMER = NS + "-ack-timer-";

    /**
     * connection 定期检测线程
     */
    public static final String T_NETTY_TIMER = NS + "-timer-";

}
