package com.mpush.api.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FutureListener extends CompletableFuture<Boolean> implements Listener {
    private final Listener l;// 防止Listener被重复执行
    private final AtomicBoolean started;// 防止Listener被重复执行

    public FutureListener(AtomicBoolean started) {
        this.started = started;
        this.l = null;
    }

    public FutureListener(Listener l, AtomicBoolean started) {
        this.l = l;
        this.started = started;
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
        throw cause instanceof ServiceException
                ? (ServiceException) cause
                : new ServiceException(cause);
    }

    public void monitor(BaseService service) {
        if (isDone()) return;
        runAsync(() -> {
            try {
                this.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                this.onFailure(new ServiceException(String.format("service %s monitor timeout", service.getClass().getSimpleName())));
            }
        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }


}