package com.mpush.bootstrap;

import com.mpush.tools.log.Logs;

public class Main {

    public static void main(String[] args) {
        Logs.init();
        Logs.Console.info("launch app...");
        ServerLauncher launcher = new ServerLauncher();
        launcher.start();
        addHook(launcher);
    }

    private static void addHook(final ServerLauncher serverBoot) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                serverBoot.stop();
                Logs.Console.info("jvm exit all server stopped...");
            }
        });
    }

}
