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

package com.mpush.tools.log;

import com.mpush.tools.config.CC;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by ohun on 2016/5/16.
 *
 * @author ohun@live.cn
 */
public interface Logs {
    boolean logInit = init();

    static boolean init() {
        if (logInit) return true;
        System.setProperty("log.home", CC.mp.log_dir);
        System.setProperty("log.root.level", CC.mp.log_level);
        System.setProperty("logback.configurationFile", CC.mp.log_conf_path);
        LoggerFactory
                .getLogger("console")
                .info(CC.mp.cfg.root().render(ConfigRenderOptions.concise().setFormatted(true)));
        return true;
    }

    Logger Console = LoggerFactory.getLogger("console"),

    CONN = LoggerFactory.getLogger("mpush.conn.log"),

    MONITOR = LoggerFactory.getLogger("mpush.monitor.log"),

    PUSH = LoggerFactory.getLogger("mpush.push.log"),

    HB = LoggerFactory.getLogger("mpush.heartbeat.log"),

    CACHE = LoggerFactory.getLogger("mpush.cache.log"),

    RSD = LoggerFactory.getLogger("mpush.srd.log"),

    HTTP = LoggerFactory.getLogger("mpush.http.log"),

    PROFILE = LoggerFactory.getLogger("mpush.profile.log");
}
