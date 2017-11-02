package com.mpush.api.spi.push;

import com.mpush.api.spi.Plugin;

public interface PushListener<T extends IPushMessage> extends Plugin {

    /**
     * 消息下发成功后回调
     * 如果消息需要ACK则该方法不会被调用
     *
     * @param message    要下发的消息
     * @param timePoints 消息流转时间节点
     */
    void onSuccess(T message, Object[] timePoints);

    /**
     * 收到客户端ACK后回调
     *
     * @param message    要下发的消息
     * @param timePoints 消息流转时间节点
     */
    void onAckSuccess(T message, Object[] timePoints);

    /**
     * 广播消息推送全部结束后回调
     *
     * @param message    要下发的消息
     * @param timePoints 消息流转时间节点
     */
    void onBroadcastComplete(T message, Object[] timePoints);

    /**
     * 消息下发失败后回调
     *
     * @param message    要下发的消息
     * @param timePoints 消息流转时间节点
     */
    void onFailure(T message, Object[] timePoints);

    /**
     * 推送消息发现用户不在线时回调
     *
     * @param message    要下发的消息
     * @param timePoints 消息流转时间节点
     */
    void onOffline(T message, Object[] timePoints);

    /**
     * 推送消息发现用户不在当前机器时回调
     *
     * @param message    要下发的消息
     * @param timePoints 消息流转时间节点
     */
    void onRedirect(T message, Object[] timePoints);

    /**
     * 发送消息超时或等待客户端ACK超时时回调
     *
     * @param message    要下发的消息
     * @param timePoints 消息流转时间节点
     */
    void onTimeout(T message, Object[] timePoints);
}