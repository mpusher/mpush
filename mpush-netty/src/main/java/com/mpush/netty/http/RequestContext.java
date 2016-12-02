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

package com.mpush.netty.http;

import com.google.common.primitives.Ints;
import com.mpush.api.Constants;
import com.mpush.tools.config.CC;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.atomic.AtomicBoolean;

public class RequestContext implements TimerTask, HttpCallback {
    private static final int TIMEOUT = CC.mp.http.default_read_timeout;
    private final long startTime = System.currentTimeMillis();
    final AtomicBoolean cancelled = new AtomicBoolean(false);
    final int readTimeout;
    private long endTime = startTime;
    private String uri;
    private HttpCallback callback;
    FullHttpRequest request;
    String host;

    public RequestContext(FullHttpRequest request, HttpCallback callback) {
        this.callback = callback;
        this.request = request;
        this.uri = request.uri();
        this.readTimeout = parseTimeout();
    }

    private int parseTimeout() {
        String timeout = request.headers().get(Constants.HTTP_HEAD_READ_TIMEOUT);
        if (timeout != null) {
            request.headers().remove(Constants.HTTP_HEAD_READ_TIMEOUT);
            Integer integer = Ints.tryParse(timeout);
            if (integer != null && integer > 0) {
                return integer;
            }
        }
        return TIMEOUT;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        if (tryDone()) {
            if (callback != null) {
                callback.onTimeout();
            }
        }
    }

    /**
     * 由于检测请求超时的任务存在，为了防止多线程下重复处理
     *
     * @return
     */
    public boolean tryDone() {
        return cancelled.compareAndSet(false, true);
    }

    @Override
    public String toString() {
        return "RequestInfo{" +
                "cancelled=" + cancelled +
                ", uri='" + uri + '\'' +
                ", host='" + host + '\'' +
                ", readTimeout=" + readTimeout +
                ", costTime=" + (endTime - startTime) +
                '}';
    }

    @Override
    public void onResponse(HttpResponse response) {
        callback.onResponse(response);
        endTime = System.currentTimeMillis();
        destroy();
    }

    @Override
    public void onFailure(int statusCode, String reasonPhrase) {
        callback.onFailure(statusCode, reasonPhrase);
        endTime = System.currentTimeMillis();
        destroy();
    }

    @Override
    public void onException(Throwable throwable) {
        callback.onException(throwable);
        endTime = System.currentTimeMillis();
        destroy();
    }

    @Override
    public void onTimeout() {
        callback.onTimeout();
        endTime = System.currentTimeMillis();
        destroy();
    }

    @Override
    public boolean onRedirect(HttpResponse response) {
        endTime = System.currentTimeMillis();
        return callback.onRedirect(response);
    }

    private void destroy() {
        request = null;
        callback = null;
    }
}