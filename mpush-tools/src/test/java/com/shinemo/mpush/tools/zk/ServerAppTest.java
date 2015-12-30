package com.shinemo.mpush.tools.zk;

import java.util.UUID;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.UriSpec;

public class ServerAppTest {

	public static void main(String[] args) throws Exception {
		CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(10000, 3));

		client.getCuratorListenable().addListener(new CuratorListener() {

			@Override
			public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
				System.out.println("Node data is changed, new data: " + new String(event.getData()));
			}
		});

		client.start();
		final ServiceRegistrar serviceRegistrar = new ServiceRegistrar(client, "services");
		final ServiceInstance<InstanceDetails> instance1 = ServiceInstance.<InstanceDetails> builder().name("service1").port(12345).address("192.168.1.100").serviceType(ServiceType.DYNAMIC)
				.payload(new InstanceDetails(UUID.randomUUID().toString(), "192.168.1.100", 12345, "Test.Service1")).uriSpec(new UriSpec("{scheme}://{address}:{port}")).build();
		final ServiceInstance<InstanceDetails> instance2 = ServiceInstance.<InstanceDetails> builder().name("service2").port(12345).address("192.168.1.100").serviceType(ServiceType.DYNAMIC)
				.payload(new InstanceDetails(UUID.randomUUID().toString(), "192.168.1.100", 12345, "Test.Service2")).uriSpec(new UriSpec("{scheme}://{address}:{port}")).build();
		serviceRegistrar.registerService(instance1);
		System.out.println("register instance1");
		serviceRegistrar.registerService(instance2);
		System.out.println("register instance2");
		// 删除
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					serviceRegistrar.unregisterService(instance1);
					System.out.println("unregister instance1");
					serviceRegistrar.unregisterService(instance2);
					System.out.println("unregister instance2");
				} catch (Exception e) {
				}
			}
		});

		Thread.sleep(Integer.MAX_VALUE);
	}

}
