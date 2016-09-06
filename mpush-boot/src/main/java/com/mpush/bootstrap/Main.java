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

package com.mpush.bootstrap;

import com.mpush.tools.log.Logs;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Main {
    public static void main(String[] args) {
        Logs.init();
        Logs.Console.info("launch mpush server...");
        ServerLauncher launcher = new ServerLauncher();
        launcher.start();
        addHook(launcher);
    }

    private static void addHook(final ServerLauncher launcher) {
        Hook hook = new Hook(launcher);
        //Signal.handle(new Signal("USR2"), hook);
        Runtime.getRuntime().addShutdownHook(new Thread(hook, "mpush-hook-thread"));
    }

    private static class Hook implements Runnable, SignalHandler {
        private final ServerLauncher launcher;

        private Hook(ServerLauncher launcher) {
            this.launcher = launcher;
        }

        @Override
        public void run() {
            stop();
        }

        @Override
        public void handle(Signal signal) {
            stop();
        }

        private void stop() {
            try {
                launcher.stop();
            } catch (Exception e) {
                Logs.Console.error("mpush server stop ex", e);
            }
            Logs.Console.error("jvm exit, all server stopped...");
        }
    }
}
