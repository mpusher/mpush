/*
  * (C) Copyright 2015-2016 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * Contributors:
  *     ohun@live.cn (夜色)
  */
package com.mpush.tools.common;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yxx on 2016/5/26.
 *
 * @author ohun@live.cn (夜色)
 */
public final class TimeLine {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final TimePoint root = new TimePoint("root");
    private final String name;
    private TimePoint current = root;

    public TimeLine() {
        name = "TimeLine";
    }

    public TimeLine(String name) {
        this.name = name;
    }

    public void begin() {
        addTimePoint("begin");
    }

    public void addTimePoint(String name) {
        current = current.next = new TimePoint(name);
    }

    public void end() {
        addTimePoint("end");
    }

    public void clean() {
        root.next = null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (root.next != null) {
            sb.append('[').append(current.point - root.next.point).append(']');
        }
        sb.append('{');
        TimePoint next = root;
        while ((next = next.next) != null) {
            sb.append(next.toString());
        }
        sb.append('}');
        return sb.toString();
    }

    private static class TimePoint {
        private final String name;
        private final long point = System.currentTimeMillis();
        private TimePoint next;

        public TimePoint(String name) {
            this.name = name;
        }

        public void setNext(TimePoint next) {
            this.next = next;
        }

        @Override
        public String toString() {
            String header = name + "[" + formatter.format(new Date(point)) + "]";
            if (next == null) return header;
            return header + " --" + (next.point - point) + "(ms)--> ";
        }
    }
}
