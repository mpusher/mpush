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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mpush.tools.thread.ThreadNames.THREAD_NAME_PREFIX;

/**
 * Created by xiaoxu.yxx on 2015/7/19.
 */
public final class NamedThreadFactory implements ThreadFactory {
    protected final AtomicInteger threadNumber = new AtomicInteger(1);
    protected final String namePrefix;
    protected final ThreadGroup group;


    public NamedThreadFactory() {
        this(THREAD_NAME_PREFIX);
    }

    public NamedThreadFactory(final String namePrefix) {
        this.namePrefix = namePrefix;
        this.group = Thread.currentThread().getThreadGroup();
    }

    public Thread newThread(String name, Runnable r) {
        return new Thread(group, r, name);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = newThread(namePrefix + threadNumber.getAndIncrement(), r);
        if (t.isDaemon())
            t.setDaemon(false);
        return t;
    }
}
