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
    private final TimePoint root = new TimePoint("root");
    private final String name;
    private int pointCount;
    private TimePoint current = root;

    public TimeLine() {
        name = "TimeLine";
    }

    public TimeLine(String name) {
        this.name = name;
    }

    public void begin(String name) {
        addTimePoint(name);
    }

    public void begin() {
        addTimePoint("begin");
    }

    public void addTimePoint(String name) {
        current = current.next = new TimePoint(name);
        pointCount++;
    }

    public void addTimePoints(Object[] points) {
        if (points != null) {
            for (int i = 0; i < points.length; i++) {
                current = current.next = new TimePoint((String) points[i], ((Number) points[++i]).longValue());
                pointCount++;
            }
        }
    }

    public TimeLine end(String name) {
        addTimePoint(name);
        return this;
    }

    public TimeLine end() {
        addTimePoint("end");
        return this;
    }

    public TimeLine successEnd() {
        addTimePoint("success-end");
        return this;
    }

    public TimeLine failureEnd() {
        addTimePoint("failure-end");
        return this;
    }

    public TimeLine timeoutEnd() {
        addTimePoint("timeout-end");
        return this;
    }

    public void clean() {
        root.next = null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (root.next != null) {
            sb.append('[').append(current.time - root.next.time).append(']').append("(ms)");
        }
        sb.append('{');
        TimePoint next = root;
        while ((next = next.next) != null) {
            sb.append(next.toString());
        }
        sb.append('}');
        return sb.toString();
    }

    public Object[] getTimePoints() {
        Object[] arrays = new Object[2 * pointCount];
        int i = 0;
        TimePoint next = root;
        while ((next = next.next) != null) {
            arrays[i++] = next.name;
            arrays[i++] = next.time;
        }
        return arrays;
    }

    private static class TimePoint {
        private final String name;
        private final long time;
        private transient TimePoint next;

        public TimePoint(String name) {
            this.name = name;
            this.time = System.currentTimeMillis();
        }

        public TimePoint(String name, long time) {
            this.name = name;
            this.time = time;
        }

        public void setNext(TimePoint next) {
            this.next = next;
        }

        @Override
        public String toString() {
            if (next == null) return name;
            return name + " --（" + (next.time - time) + "ms) --> ";
        }
    }
}
