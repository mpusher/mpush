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

package com.mpush.monitor.data;

import com.mpush.tools.Jsons;

import java.util.HashMap;
import java.util.Map;

public class MonitorResult {
    private Long timestamp = System.currentTimeMillis();
    private Map<String, Object> results = new HashMap<>(8);

    public MonitorResult addResult(String name, Object result) {
        results.put(name, result);
        return this;
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public MonitorResult setResults(Map<String, Object> results) {
        this.results = results;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public MonitorResult setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return "MonitorResult{" +
                "results=" + results +
                ", timestamp=" + timestamp +
                '}';
    }

    public String toJson() {
        return Jsons.toJson(this);
    }
}
