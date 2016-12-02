package com.mpush.monitor.jmx.stats;

import java.util.Date;

/**
 * Statistics on the ServerCnxn
 */
interface Stats {
    /**
     * Date/time the connection was established
     *
     * @since 3.3.0
     */
    Date getEstablished();

    /**
     * The number of requests that have been submitted but not yet
     * responded to.
     */
    long getOutstandingRequests();

    /**
     * Number of packets received
     */
    long getPacketsReceived();

    /**
     * Number of packets sent (incl notifications)
     */
    long getPacketsSent();

    /**
     * Min latency in ms
     *
     * @since 3.3.0
     */
    long getMinLatency();

    /**
     * Average latency in ms
     *
     * @since 3.3.0
     */
    long getAvgLatency();

    /**
     * Max latency in ms
     *
     * @since 3.3.0
     */
    long getMaxLatency();

    /**
     * Last operation performed by this connection
     *
     * @since 3.3.0
     */
    String getLastOperation();

    /**
     * Last cxid of this connection
     *
     * @since 3.3.0
     */
    long getLastCxid();

    /**
     * Last zxid of this connection
     *
     * @since 3.3.0
     */
    long getLastZxid();

    /**
     * Last time server sent a response to client on this connection
     *
     * @since 3.3.0
     */
    long getLastResponseTime();

    /**
     * Latency of last response to client on this connection in ms
     *
     * @since 3.3.0
     */
    long getLastLatency();

    /**
     * Reset counters
     *
     * @since 3.3.0
     */
    void resetStats();
}