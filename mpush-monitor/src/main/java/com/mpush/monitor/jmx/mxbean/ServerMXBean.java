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

package com.mpush.monitor.jmx.mxbean;

/**
 * Created by ohun on 16/10/23.
 *
 * @author ohun@live.cn (夜色)
 */
public interface ServerMXBean {
    /**
     * @return the server socket port number
     */
    String getClientPort();

    /**
     * @return the server version
     */
    String getVersion();

    /**
     * @return time the server was started
     */
    String getStartTime();

    /**
     * @return min request latency in ms
     */
    long getMinRequestLatency();

    /**
     * @return average request latency in ms
     */
    long getAvgRequestLatency();

    /**
     * @return max request latency in ms
     */
    long getMaxRequestLatency();

    /**
     * @return number of packets received so far
     */
    long getPacketsReceived();

    /**
     * @return number of packets sent so far
     */
    long getPacketsSent();

    /**
     * @return number of outstanding requests.
     */
    long getOutstandingRequests();

    /**
     * Current TickTime of server in milliseconds
     */
    int getTickTime();

    /**
     * Set TickTime of server in milliseconds
     */
    void setTickTime(int tickTime);

    /**
     * Current maxClientCnxns allowed from a particular host
     */
    int getMaxClientCnxnsPerHost();

    /**
     * Set maxClientCnxns allowed from a particular host
     */
    void setMaxClientCnxnsPerHost(int max);

    /**
     * Current minSessionTimeout of the server in milliseconds
     */
    int getMinSessionTimeout();

    /**
     * Set minSessionTimeout of server in milliseconds
     */
    void setMinSessionTimeout(int min);

    /**
     * Current maxSessionTimeout of the server in milliseconds
     */
    int getMaxSessionTimeout();

    /**
     * Set maxSessionTimeout of server in milliseconds
     */
    void setMaxSessionTimeout(int max);

    /**
     * Reset packet and latency statistics
     */
    void resetStatistics();

    /**
     * Reset min/avg/max latency statistics
     */
    void resetLatency();

    /**
     * Reset max latency statistics only.
     */
    void resetMaxLatency();

    /**
     * @return number of alive client connections
     */
    long getNumAliveConnections();
}
