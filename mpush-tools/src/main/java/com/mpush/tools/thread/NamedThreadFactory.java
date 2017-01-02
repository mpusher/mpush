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
 *
 * @author ohun@live.cn (夜色)
 */
public final class NamedThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final ThreadGroup group;


    public NamedThreadFactory() {
        this(THREAD_NAME_PREFIX);
    }

    public NamedThreadFactory(final String namePrefix) {
        this.namePrefix = namePrefix;
        this.group = Thread.currentThread().getThreadGroup();
    }

    /**
     * Daemon的作用是为其他线程的运行提供服务，比如说GC线程。其实User Thread线程和Daemon Thread守护线程本质上来说去没啥区别的，
     * 唯一的区别之处就在虚拟机的离开：如果User Thread全部撤离，那么Daemon Thread也就没啥线程好服务的了，所以虚拟机也就退出了。
     * 守护线程并非虚拟机内部可以提供，用户也可以自行的设定守护线程，方法：public final void setDaemon(boolean on) ；
     * <p>
     * 但是有几点需要注意：
     * 1）、thread.setDaemon(true)必须在thread.start()之前设置，否则会跑出一个IllegalThreadStateException异常。你不能把正在运行的常规线程设置为守护线程。
     * <p>
     * 2）、 在Daemon线程中产生的新线程也是Daemon的。
     * <p>
     * 3）、不是所有的应用都可以分配给Daemon线程来进行服务，比如读写操作或者计算逻辑。因为在Daemon Thread还没来的及进行操作时，虚拟机可能已经退出了。
     *
     * @param name name
     * @param r    runnable
     * @return new Thread
     */
    public Thread newThread(String name, Runnable r) {
        Thread thread = new Thread(group, r, namePrefix + "-" + threadNumber.getAndIncrement() + "-" + name);
        thread.setDaemon(true); //设置为非守护线程，否则jvm会立即退出
        return thread;
    }

    @Override
    public Thread newThread(Runnable r) {
        return newThread("none", r);
    }

    public static NamedThreadFactory build() {
        return new NamedThreadFactory();
    }

    public static NamedThreadFactory build(String namePrefix) {
        return new NamedThreadFactory(namePrefix);
    }
}
