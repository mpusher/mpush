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

package com.mpush.test.udp;

import com.mpush.tools.Utils;
import org.junit.Test;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by ohun on 16/10/21.
 *
 * @author ohun@live.cn (夜色)
 */
public final class MulticastTest {
    @Test
    public void TestServer() throws Exception {
        //接受组播和发送组播的数据报服务都要把组播地址添加进来
        String host = "239.239.239.88";//多播地址
        int port = 9998;
        InetAddress group = InetAddress.getByName(host);

        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);
        channel.bind(new InetSocketAddress(port));
        channel.join(group, Utils.getLocalNetworkInterface());
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        SocketAddress sender = channel.receive(buffer);
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        System.out.println(new String(data));

    }

    @Test
    public void testSend() throws Exception {
        String host = "239.239.239.99";//多播地址
        int port = 9999;
        InetAddress group = InetAddress.getByName(host);
        String message = "test-multicastSocket";

        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);
        channel.configureBlocking(true);
        channel.bind(new InetSocketAddress(port));
        channel.join(group, Utils.getLocalNetworkInterface());

        InetSocketAddress sender = new InetSocketAddress("239.239.239.99", 4000);
        channel.send(ByteBuffer.wrap(message.getBytes()), sender);

        channel.close();
    }
}
