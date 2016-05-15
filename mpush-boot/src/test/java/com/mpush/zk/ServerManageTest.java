//package com.mpush.core.zk;
//
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import MPushUtil;
//import com.mpush.tools.zk.ServerApp;
//import com.mpush.tools.zk.ZKPath;
//import com.mpush.tools.zk.manage.ServerManage;
//
//public class ServerManageTest {
//
//    private static Executor executor = Executors.newCachedThreadPool();
//
//    private ServerApp app = new ServerApp(MPushUtil.getLocalIp(), 3000);
//
//    private ServerManage manage = new ServerManage(app, ZKPath.CONNECTION_SERVER);
//
//    @Test
//    public void testMulThreadRegisterApp() throws InterruptedException {
//        CountDownLatch latch = new CountDownLatch(1);
//        for (int i = 1; i <= 2; i++) {
//            executor.execute(new Worker("192.168.1." + i, latch));
//        }
//        latch.countDown();
//
//        Thread.sleep(Integer.MAX_VALUE);
//    }
//
//
//    @Test
//    public void testServerManageStart() throws InterruptedException {
//        manage.start();
//        Thread.sleep(Integer.MAX_VALUE);
//    }
//
//
//    private static class Worker implements Runnable {
//
//        private static final Logger log = LoggerFactory.getLogger(Worker.class);
//
//        private final String ip;
//        private final CountDownLatch latch;
//
//        public Worker(String ip, CountDownLatch latch) {
//            this.ip = ip;
//            this.latch = latch;
//        }
//
//        @Override
//        public void run() {
//            try {
//                latch.await();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            log.warn("start init " + ip);
//            ServerApp app = new ServerApp(ip, 3000);
//            ServerManage manage = new ServerManage(app, ZKPath.CONNECTION_SERVER);
//            manage.start();
//
//            try {
//                Thread.sleep(20000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            manage.close();
//
//            log.warn("end init " + ip);
//        }
//
//    }
//
//}
