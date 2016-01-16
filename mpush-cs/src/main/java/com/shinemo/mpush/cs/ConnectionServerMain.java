package com.shinemo.mpush.cs;



import com.shinemo.mpush.api.Server;
import com.shinemo.mpush.core.AbstractServer;
import com.shinemo.mpush.core.server.ConnectionServer;
import com.shinemo.mpush.cs.zk.listener.impl.ConnectionServerPathListener;
import com.shinemo.mpush.cs.zk.listener.impl.PushServerPathListener;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.zk.listener.impl.RedisPathListener;

public class ConnectionServerMain extends AbstractServer<ConnectionServerApplication>{
	
	public ConnectionServerMain(){
		
		registerListener(new RedisPathListener());
		registerListener(new PushServerPathListener());
		registerListener(new ConnectionServerPathListener());
		
	}

	@Override
	public Server getServer() {
		final int port = ConfigCenter.holder.connectionServerPort();
        ConnectionServer connectionServer = new ConnectionServer(port);
        return connectionServer;
	}

	
	public static void main(String[] args) {
		final ConnectionServerMain connectionServerMain = new ConnectionServerMain();
		connectionServerMain.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	connectionServerMain.stop();
            }
        });
	}

}
