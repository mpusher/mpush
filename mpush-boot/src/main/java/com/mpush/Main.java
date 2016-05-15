package com.mpush;

import com.mpush.tools.ConsoleLog;

public class Main {

    public static void main(String[] args) {
        ConsoleLog.i("launch app...");
        ServerLauncher launcher = new ServerLauncher();
        launcher.start();
        addHook(launcher);
    }

    private static void addHook(final ServerLauncher serverBoot) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                serverBoot.stop();
                ConsoleLog.i("jvm exit all server stopped...");
            }
        });
    }

}
