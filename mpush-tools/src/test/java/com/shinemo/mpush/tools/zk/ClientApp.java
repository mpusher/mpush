package com.shinemo.mpush.tools.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceInstance;

public class ClientApp {

    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000, 3));
        
        client.getCuratorListenable().addListener(new CuratorListener() {
			
			@Override
			public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
				System.out.println("Node data is changed, new data: " + new String(event.getData()));
			}
			
		});
        
        client.start();
        ServiceDiscoverer serviceDiscoverer = new ServiceDiscoverer(client,"services");

        ServiceInstance<InstanceDetails> instance1 = serviceDiscoverer.getInstanceByName("service1");

        if(instance1!=null){
            System.out.println(instance1.buildUriSpec());
            System.out.println(instance1.getPayload());
        }else{
        	System.out.println("instance1 is null");
        }

        ServiceInstance<InstanceDetails> instance2 = serviceDiscoverer.getInstanceByName("service1");

        if(instance2!=null){
            System.out.println(instance2.buildUriSpec());
            System.out.println(instance2.getPayload());
        }else{
        	System.out.println("instance2 is null");
        }

        serviceDiscoverer.close();
        CloseableUtils.closeQuietly(client);
    }
}
