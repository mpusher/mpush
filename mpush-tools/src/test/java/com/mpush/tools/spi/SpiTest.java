package com.mpush.tools.spi;


import com.mpush.tools.spi.test.TestService;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class SpiTest {

    private static Executor pool = Executors.newCachedThreadPool();

    @Test
    public void baseTest() {
        TestService testService = ServiceContainer.load(TestService.class);
        System.out.println(testService.sayHi(" huang"));

        ServiceContainer.load(TestService.class);
    }

    @Ignore
    @Test
    public void listTest() {


    }

    @Ignore
    @Test
    public void mulThreadTest() throws InterruptedException {
        pool.execute(new Worker());
        pool.execute(new Worker());
        pool.execute(new ListWorker());
        pool.execute(new ListWorker());
        Thread.sleep(Integer.MAX_VALUE);
    }


    private static final class Worker implements Runnable {

        @Override
        public void run() {
        }

    }

    private static final class ListWorker implements Runnable {

        @Override
        public void run() {


        }

    }

}
