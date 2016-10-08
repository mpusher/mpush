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

package com.mpush.api.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yxx on 2016/5/19.
 *
 * @author ohun@live.cn
 */
public abstract class BaseService implements Service {

    protected final AtomicBoolean started = new AtomicBoolean();

    @Override
    public void init() {
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    protected void tryStart(Listener listener, Function function) {
        listener = wrap(listener);
        if (started.compareAndSet(false, true)) {
            try {
                init();
                function.apply(listener);
                listener.onSuccess(String.format("service %s start success", this.getClass().getSimpleName()));
            } catch (Throwable e) {
                listener.onFailure(e);
                throw new ServiceException(e);
            }
        } else {
            listener.onFailure(new ServiceException("service already started."));
        }
    }

    protected void tryStop(Listener listener, Function function) {
        listener = wrap(listener);
        if (started.compareAndSet(true, false)) {
            try {
                function.apply(listener);
                listener.onSuccess(String.format("service %s stop success", this.getClass().getSimpleName()));
            } catch (Throwable e) {
                listener.onFailure(e);
                throw new ServiceException(e);
            }
        } else {
            listener.onFailure(new ServiceException("service already stopped."));
        }
    }

    public final CompletableFuture<Boolean> start() {
        FutureListener listener = new FutureListener();
        start(listener);
        return listener;
    }

    public final CompletableFuture<Boolean> stop() {
        FutureListener listener = new FutureListener();
        stop(listener);
        return listener;
    }

    @Override
    public void start(Listener listener) {
        tryStart(listener, this::doStart);
    }

    @Override
    public void stop(Listener listener) {
        tryStop(listener, this::doStop);
    }

    protected abstract void doStart(Listener listener) throws Throwable;

    protected abstract void doStop(Listener listener) throws Throwable;

    protected interface Function {
        void apply(Listener l) throws Throwable;
    }

    /**
     * 防止Listener被重复执行
     *
     * @param l
     * @return
     */
    public FutureListener wrap(Listener l) {
        if (l == null) return new FutureListener();
        if (l instanceof FutureListener) return (FutureListener) l;
        return new FutureListener(l);
    }

    protected class FutureListener extends CompletableFuture<Boolean> implements Listener {
        private final Listener l;// 防止Listener被重复执行

        public FutureListener() {
            this.l = null;
        }

        public FutureListener(Listener l) {
            this.l = l;
        }

        @Override
        public void onSuccess(Object... args) {
            if (isDone()) return;// 防止Listener被重复执行
            complete(started.get());
            if (l != null) l.onSuccess(args);
        }

        @Override
        public void onFailure(Throwable cause) {
            if (isDone()) return;// 防止Listener被重复执行
            completeExceptionally(cause);
            if (l != null) l.onFailure(cause);
            throw new ServiceException(cause);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException();
        }
    }
}
