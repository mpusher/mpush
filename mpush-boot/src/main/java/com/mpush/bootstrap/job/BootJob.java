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

package com.mpush.bootstrap.job;

import com.mpush.tools.log.Logs;

import java.util.function.Supplier;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public abstract class BootJob {
    protected BootJob next;

    protected abstract void start();

    protected abstract void stop();

    public void startNext() {
        if (next != null) {
            Logs.Console.info("start bootstrap job [{}]", getNextName());
            next.start();
        }
    }

    public void stopNext() {
        if (next != null) {
            next.stop();
            Logs.Console.info("stopped bootstrap job [{}]", getNextName());
        }
    }

    public BootJob setNext(BootJob next) {
        this.next = next;
        return next;
    }

    public BootJob setNext(Supplier<BootJob> next, boolean enabled) {
        if (enabled) {
            return setNext(next.get());
        }
        return this;
    }

    protected String getNextName() {
        return next.getName();
    }

    protected String getName() {
        return this.getClass().getSimpleName();
    }
}
