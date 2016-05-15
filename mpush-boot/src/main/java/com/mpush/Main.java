package com.mpush;

public class Main {

    public static void main(String[] args) {
        ServerLauncher launcher = new ServerLauncher();
        launcher.start();
        addHook(launcher);
    }

    private static void addHook(final ServerLauncher serverBoot) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                serverBoot.stop();
            }
        });
    }

}
