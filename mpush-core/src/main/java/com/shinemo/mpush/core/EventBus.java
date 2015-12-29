
package com.shinemo.mpush.core;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.shinemo.mpush.api.event.Event;
import com.shinemo.mpush.tools.thread.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * Created by ohun on 2015/12/29.
 */
public class EventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventBus.class);
    public static final EventBus INSTANCE = new EventBus();
    private final com.google.common.eventbus.EventBus eventBus;

    public EventBus() {
        Executor executor = ThreadPoolUtil.getThreadPoolManager().getThreadExecutor("event-bus-pool", 4, 4);
        eventBus = new AsyncEventBus(executor, new SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                LOGGER.error("event bus subscriber ex", exception);
            }
        });
    }

    public void post(Event event) {
        eventBus.post(event);
    }


    public void register(Object bean) {
        eventBus.register(bean);
    }

    public void unregister(Object bean) {
        eventBus.unregister(bean);
    }

}
