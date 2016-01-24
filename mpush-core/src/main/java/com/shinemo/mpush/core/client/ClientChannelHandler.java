package com.shinemo.mpush.core.client;



import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.common.message.BindUserMessage;
import com.shinemo.mpush.common.message.ErrorMessage;
import com.shinemo.mpush.common.message.FastConnectOkMessage;
import com.shinemo.mpush.common.message.HandshakeMessage;
import com.shinemo.mpush.common.message.HandshakeOkMessage;
import com.shinemo.mpush.common.message.KickUserMessage;
import com.shinemo.mpush.common.message.OkMessage;
import com.shinemo.mpush.common.message.PushMessage;
import com.shinemo.mpush.common.security.AesCipher;
import com.shinemo.mpush.common.security.CipherBox;
import com.shinemo.mpush.netty.client.ChannelClientHandler;
import com.shinemo.mpush.netty.client.NettyClientFactory;
import com.shinemo.mpush.netty.client.SecurityNettyClient;
import com.shinemo.mpush.netty.connection.NettyConnection;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/19.
 */
@ChannelHandler.Sharable
public final class ClientChannelHandler extends ChannelHandlerAdapter implements ChannelClientHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientChannelHandler.class);
    
    private Client client;
    
    public ClientChannelHandler(Client client) {
    	this.client = client;
	}
    
    @Override
    public Client getClient() {
    	return client;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Client client = NettyClientFactory.INSTANCE.getCientByChannel(ctx.channel());
        client.getConnection().updateLastReadTime();
        if(client instanceof SecurityNettyClient){
        	
        	SecurityNettyClient securityNettyClient = (SecurityNettyClient)client;
        	
        	Connection connection = client.getConnection();
        	//加密
        	if (msg instanceof Packet) {
                Packet packet = (Packet) msg;
                Command command = Command.toCMD(packet.cmd);
                if (command == Command.HANDSHAKE) {
                    connection.getSessionContext().changeCipher(new AesCipher(securityNettyClient.getClientKey(), securityNettyClient.getIv()));
                    HandshakeOkMessage message = new HandshakeOkMessage(packet, connection);
                    byte[] sessionKey = CipherBox.INSTANCE.mixKey(securityNettyClient.getClientKey(), message.serverKey);
                    connection.getSessionContext().changeCipher(new AesCipher(sessionKey, securityNettyClient.getIv()));
                    client.startHeartBeat();
                    LOGGER.info("会话密钥：{}，message={}", sessionKey, message);
                    bindUser(securityNettyClient);
                } else if (command == Command.FAST_CONNECT) {
                    String cipherStr = securityNettyClient.getCipher();
                    String[] cs = cipherStr.split(",");
                    byte[] key = AesCipher.toArray(cs[0]);
                    byte[] iv = AesCipher.toArray(cs[1]);
                    connection.getSessionContext().changeCipher(new AesCipher(key, iv));

                    FastConnectOkMessage message = new FastConnectOkMessage(packet, connection);
                    client.startHeartBeat();
                    bindUser(securityNettyClient);
                    LOGGER.info("fast connect success, message=" + message);
                } else if (command == Command.KICK) {
                    KickUserMessage message = new KickUserMessage(packet, connection);
                    LOGGER.error("receive kick user userId={}, deviceId={}, message={},",securityNettyClient.getUserId() , securityNettyClient.getDeviceId(), message);
                    ctx.close();
                } else if (command == Command.ERROR) {
                    ErrorMessage errorMessage = new ErrorMessage(packet, connection);
                    LOGGER.error("receive an error packet=" + errorMessage);
                } else if (command == Command.BIND) {
                    OkMessage okMessage = new OkMessage(packet, connection);
                    LOGGER.info("receive an success packet=" + okMessage);
                } else if (command == Command.PUSH) {
                    PushMessage message = new PushMessage(packet, connection);
                    LOGGER.info("receive an push message, content=" + message.content);
                }else if(command == Command.HEARTBEAT){
//                	connection.send(packet);  // ping -> pong
                	LOGGER.info("receive an heart beat message");
                }else{
                	LOGGER.info("receive an  message, type=" + command.cmd+","+packet);
                }
            }
        	
        }else{
        	//不加密
        }
    	LOGGER.warn("update currentTime:"+ctx.channel()+","+ToStringBuilder.reflectionToString(msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        NettyClientFactory.INSTANCE.remove(ctx.channel());
        LOGGER.error("caught an ex, channel={}", ctx.channel(), cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("client connect channel={}", ctx.channel());
        Connection connection = new NettyConnection();
        NettyClientFactory.INSTANCE.put(ctx.channel(), client);
        
        if(client instanceof SecurityNettyClient){
        	connection.init(ctx.channel(), true);
            client.initConnection(connection);
            tryFastConnect((SecurityNettyClient)client);
        }else{
        	LOGGER.error("connection is not support appear hear:"+ client);
        	connection.init(ctx.channel(), false);
            client.initConnection(connection);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("client disconnect channel={}", ctx.channel());
        NettyClientFactory.INSTANCE.remove(ctx.channel());;
    }
    
    private void tryFastConnect(SecurityNettyClient securityNettyClient) {
    	handshake(securityNettyClient);
//        if (sessionTickets == null) {
//            
//            return;
//        }
//        String sessionId = (String) sessionTickets.get("sessionId");
//        if (sessionId == null) {
//            handshake();
//            return;
//        }
//        String expireTime = (String) sessionTickets.get("expireTime");
//        if (expireTime != null) {
//            long exp = Long.parseLong(expireTime);
//            if (exp < System.currentTimeMillis()) {
//                handshake();
//                return;
//            }
//        }
//        FastConnectMessage message = new FastConnectMessage(connection);
//        message.deviceId = deviceId;
//        message.sessionId = sessionId;
//        message.send(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                if (!channelFuture.isSuccess()) {
//                    handshake();
//                }
//            }
//        });
    }
    
    private void bindUser(SecurityNettyClient client) {
        BindUserMessage message = new BindUserMessage(client.getConnection());
        message.userId = client.getUserId();
        message.send();
    }

//    private void saveToken(HandshakeOkMessage message, SessionContext context) {
//        try {
//            Map<String, Serializable> map = new HashMap<>();
//            map.put("sessionId", message.sessionId);
//            map.put("serverHost", message.serverHost);
//            map.put("expireTime", Long.toString(message.expireTime));
//            map.put("cipher", context.cipher.toString());
//            map.put("deviceId", deviceId);
//            map.put("userId", userId);
//            String path = this.getClass().getResource("/").getFile();
//            FileOutputStream out = new FileOutputStream(new File(path, "token.dat"));
//            out.write(Jsons.toJson(map).getBytes(Constants.UTF_8));
//            out.close();
//        } catch (Exception e) {
//        }
//    }
    
    private void handshake(SecurityNettyClient client) {
      HandshakeMessage message = new HandshakeMessage(client.getConnection());
      message.clientKey = client.getClientKey();
      message.iv = client.getIv();
      message.clientVersion = client.getClientVersion();
      message.deviceId = client.getDeviceId();
      message.osName = client.getOsName();
      message.osVersion = client.getOsVersion();
      message.timestamp = System.currentTimeMillis();
      message.send();
  }
    
}