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

package com.mpush.tools.common;


import com.mpush.tools.config.CC;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用来测试并统计线程执行时间的工具。
 */
@SuppressWarnings(value = {"rawtypes", "unchecked"})
public class Profiler {
    private static volatile boolean enabled = CC.mp.monitor.profile_enabled;

    private static final ThreadLocal entryStack = new ThreadLocal();
    public static final String EMPTY_STRING = "";

    public static void enable(boolean enabled) {
        Profiler.enabled = enabled;
        reset();
    }

    /**
     * 开始计时。
     */
    public static void start() {
        start(EMPTY_STRING);
    }

    /**
     * 开始计时。
     *
     * @param message 第一个entry的信息
     */
    public static void start(String message, Object... args) {
        if (enabled) entryStack.set(new Entry(String.format(message, args), null, null));
    }

    /**
     * 开始计时。
     *
     * @param message 第一个entry的信息
     */
    public static void start(Message message) {
        if (enabled) entryStack.set(new Entry(message, null, null));
    }

    /**
     * 清除计时器。
     * <p>
     * 清除以后必须再次调用<code>start</code>方可重新计时。
     * </p>
     */
    public static void reset() {
        entryStack.remove();
    }

    /**
     * 开始一个新的entry，并计时。
     *
     * @param message 新entry的信息
     */
    public static void enter(String message) {
        if (enabled) {
            Entry currentEntry = getCurrentEntry();

            if (currentEntry != null) {
                currentEntry.enterSubEntry(message);
            }
        }
    }

    /**
     * 开始一个新的entry，并计时。
     *
     * @param message 新entry的信息
     */
    public static void enter(Message message) {
        if (enabled) {
            Entry currentEntry = getCurrentEntry();

            if (currentEntry != null) {
                currentEntry.enterSubEntry(message);
            }
        }
    }

    /**
     * 结束最近的一个entry，记录结束时间。
     */
    public static void release() {
        if (enabled) {
            Entry currentEntry = getCurrentEntry();

            if (currentEntry != null) {
                currentEntry.release();
            }
        }
    }

    /**
     * 取得耗费的总时间。
     *
     * @return 耗费的总时间，如果未开始计时，则返回<code>-1</code>
     */
    public static long getDuration() {
        if (enabled) {
            Entry entry = (Entry) entryStack.get();

            if (entry != null) {
                return entry.getDuration();
            } else {
                return -1;
            }
        }
        return -1;
    }

    /**
     * 列出所有的entry。
     *
     * @return 列出所有entry，并统计各自所占用的时间
     */
    public static String dump() {
        return dump("", "");
    }

    /**
     * 列出所有的entry。
     *
     * @param prefix 前缀
     * @return 列出所有entry，并统计各自所占用的时间
     */
    public static String dump(String prefix) {
        return dump(prefix, prefix);
    }

    /**
     * 列出所有的entry。
     *
     * @param prefix1 首行前缀
     * @param prefix2 后续行前缀
     * @return 列出所有entry，并统计各自所占用的时间
     */
    public static String dump(String prefix1, String prefix2) {
        Entry entry = (Entry) entryStack.get();

        if (entry != null) {
            return entry.toString(prefix1, prefix2);
        } else {
            return EMPTY_STRING;
        }
    }

    /**
     * 取得第一个entry。
     *
     * @return 第一个entry，如果不存在，则返回<code>null</code>
     */
    public static Entry getEntry() {
        return (Entry) entryStack.get();
    }

    /**
     * 取得最近的一个entry。
     *
     * @return 最近的一个entry，如果不存在，则返回<code>null</code>
     */
    private static Entry getCurrentEntry() {
        Entry subEntry = (Entry) entryStack.get();
        Entry entry = null;

        if (subEntry != null) {
            do {
                entry = subEntry;
                subEntry = entry.getUnreleasedEntry();
            } while (subEntry != null);
        }

        return entry;
    }

    /**
     * 代表一个计时单元。
     */
    public static final class Entry {
        private final List subEntries = new ArrayList(4);
        private final Object message;
        private final Entry parentEntry;
        private final Entry firstEntry;
        private final long baseTime;
        private final long startTime;
        private long endTime;

        /**
         * 创建一个新的entry。
         *
         * @param message     entry的信息，可以是<code>null</code>
         * @param parentEntry 父entry，可以是<code>null</code>
         * @param firstEntry  第一个entry，可以是<code>null</code>
         */
        private Entry(Object message, Entry parentEntry, Entry firstEntry) {
            this.message = message;
            this.startTime = System.currentTimeMillis();
            this.parentEntry = parentEntry;
            this.firstEntry = firstEntry == null ? this : firstEntry;
            this.baseTime = (firstEntry == null) ? 0
                    : firstEntry.startTime;
        }

        public String getMessage() {
            String messageString = null;

            if (message instanceof String) {
                messageString = (String) message;
            } else if (message instanceof Message) {
                Message messageObject = (Message) message;
                MessageLevel level = MessageLevel.BRIEF_MESSAGE;

                if (isReleased()) {
                    level = messageObject.getMessageLevel(this);
                }

                if (level == MessageLevel.DETAILED_MESSAGE) {
                    messageString = messageObject.getDetailedMessage();
                } else {
                    messageString = messageObject.getBriefMessage();
                }
            }

            return StringUtils.defaultIfEmpty(messageString, null);
        }

        /**
         * 取得entry相对于第一个entry的起始时间。
         *
         * @return 相对起始时间
         */
        public long getStartTime() {
            return (baseTime > 0) ? (startTime - baseTime)
                    : 0;
        }

        /**
         * 取得entry相对于第一个entry的结束时间。
         *
         * @return 相对结束时间，如果entry还未结束，则返回<code>-1</code>
         */
        public long getEndTime() {
            if (endTime < baseTime) {
                return -1;
            } else {
                return endTime - baseTime;
            }
        }

        /**
         * 取得entry持续的时间。
         *
         * @return entry持续的时间，如果entry还未结束，则返回<code>-1</code>
         */
        public long getDuration() {
            if (endTime < startTime) {
                return -1;
            } else {
                return endTime - startTime;
            }
        }

        /**
         * 取得entry自身所用的时间，即总时间减去所有子entry所用的时间。
         *
         * @return entry自身所用的时间，如果entry还未结束，则返回<code>-1</code>
         */
        public long getDurationOfSelf() {
            long duration = getDuration();

            if (duration < 0) {
                return -1;
            } else if (subEntries.isEmpty()) {
                return duration;
            } else {
                for (int i = 0; i < subEntries.size(); i++) {
                    Entry subEntry = (Entry) subEntries.get(i);

                    duration -= subEntry.getDuration();
                }

                if (duration < 0) {
                    return -1;
                } else {
                    return duration;
                }
            }
        }

        /**
         * 取得当前entry在父entry中所占的时间百分比。
         *
         * @return 百分比
         */
        public double getPecentage() {
            double parentDuration = 0;
            double duration = getDuration();

            if ((parentEntry != null) && parentEntry.isReleased()) {
                parentDuration = parentEntry.getDuration();
            }

            if ((duration > 0) && (parentDuration > 0)) {
                return duration / parentDuration;
            } else {
                return 0;
            }
        }

        /**
         * 取得当前entry在第一个entry中所占的时间百分比。
         *
         * @return 百分比
         */
        public double getPecentageOfAll() {
            double firstDuration = 0;
            double duration = getDuration();

            if ((firstEntry != null) && firstEntry.isReleased()) {
                firstDuration = firstEntry.getDuration();
            }

            if ((duration > 0) && (firstDuration > 0)) {
                return duration / firstDuration;
            } else {
                return 0;
            }
        }

        /**
         * 取得所有子entries。
         *
         * @return 所有子entries的列表（不可更改）
         */
        public List getSubEntries() {
            return Collections.unmodifiableList(subEntries);
        }

        /**
         * 结束当前entry，并记录结束时间。
         */
        private void release() {
            endTime = System.currentTimeMillis();
        }

        /**
         * 判断当前entry是否结束。
         *
         * @return 如果entry已经结束，则返回<code>true</code>
         */
        private boolean isReleased() {
            return endTime > 0;
        }

        /**
         * 创建一个新的子entry。
         *
         * @param message 子entry的信息
         */
        private void enterSubEntry(Object message) {
            Entry subEntry = new Entry(message, this, firstEntry);

            subEntries.add(subEntry);
        }

        /**
         * 取得未结束的子entry。
         *
         * @return 未结束的子entry，如果没有子entry，或所有entry均已结束，则返回<code>null</code>
         */
        private Entry getUnreleasedEntry() {
            Entry subEntry = null;

            if (!subEntries.isEmpty()) {
                subEntry = (Entry) subEntries.get(subEntries.size() - 1);

                if (subEntry.isReleased()) {
                    subEntry = null;
                }
            }

            return subEntry;
        }

        /**
         * 将entry转换成字符串的表示。
         *
         * @return 字符串表示的entry
         */
        public String toString() {
            return toString("", "");
        }

        /**
         * 将entry转换成字符串的表示。
         *
         * @param prefix1 首行前缀
         * @param prefix2 后续行前缀
         * @return 字符串表示的entry
         */
        private String toString(String prefix1, String prefix2) {
            StringBuilder buffer = new StringBuilder();

            toString(buffer, prefix1, prefix2);

            return buffer.toString();
        }

        /**
         * 将entry转换成字符串的表示。
         *
         * @param buffer  字符串buffer
         * @param prefix1 首行前缀
         * @param prefix2 后续行前缀
         */
        private void toString(StringBuilder buffer, String prefix1, String prefix2) {
            buffer.append(prefix1);

            String message = getMessage();
            long startTime = getStartTime();
            long duration = getDuration();
            long durationOfSelf = getDurationOfSelf();
            double percent = getPecentage();
            double percentOfAll = getPecentageOfAll();

            Object[] params = new Object[]{
                    message, // {0} - entry信息
                    new Long(startTime), // {1} - 起始时间
                    new Long(duration), // {2} - 持续总时间
                    new Long(durationOfSelf), // {3} - 自身消耗的时间
                    new Double(percent), // {4} - 在父entry中所占的时间比例
                    new Double(percentOfAll) // {5} - 在总时间中所旧的时间比例
            };

            StringBuffer pattern = new StringBuffer("{1,number} ");

            if (isReleased()) {
                pattern.append("[{2,number}ms");

                if ((durationOfSelf > 0) && (durationOfSelf != duration)) {
                    pattern.append(" ({3,number}ms)");
                }

                if (percent > 0) {
                    pattern.append(", {4,number,##%}");
                }

                if (percentOfAll > 0) {
                    pattern.append(", {5,number,##%}");
                }

                pattern.append("]");
            } else {
                pattern.append("[UNRELEASED]");
            }

            if (message != null) {
                pattern.append(" - {0}");
            }

            buffer.append(MessageFormat.format(pattern.toString(), params));

            for (int i = 0; i < subEntries.size(); i++) {
                Entry subEntry = (Entry) subEntries.get(i);

                buffer.append('\n');

                if (i == (subEntries.size() - 1)) {
                    subEntry.toString(buffer, prefix2 + "`---", prefix2 + "    "); // 最后一项
                } else if (i == 0) {
                    subEntry.toString(buffer, prefix2 + "+---", prefix2 + "|   "); // 第一项
                } else {
                    subEntry.toString(buffer, prefix2 + "+---", prefix2 + "|   "); // 中间项
                }
            }
        }
    }

    /**
     * 显示消息的级别。
     */
    public enum MessageLevel {
        NO_MESSAGE, BRIEF_MESSAGE, DETAILED_MESSAGE;
    }


    /**
     * 代表一个profiler entry的详细信息。
     */
    public interface Message {
        MessageLevel getMessageLevel(Entry entry);

        String getBriefMessage();

        String getDetailedMessage();
    }
}


