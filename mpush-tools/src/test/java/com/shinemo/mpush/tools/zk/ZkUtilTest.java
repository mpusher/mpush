package com.shinemo.mpush.tools.zk;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;
import com.shinemo.mpush.tools.Constants;
import com.shinemo.mpush.tools.InetAddressUtil;
import com.shinemo.mpush.tools.Jsons;

public class ZkUtilTest {
	
	private ZkConfig zkConfig = new ZkConfig("127.0.0.1:2181", "mpush");
	
	private ZkUtil zkUtil = new ZkUtil(zkConfig);
	
	@Before
	public void setup(){
		zkUtil.init();
	}
	
	@Test
	public void test(){
		
		String dubbo = zkUtil.get("/dubbo");
		System.out.println(dubbo);
		
		List<String> child = zkUtil.getChildrenKeys("/dubbo");
		System.out.println(ToStringBuilder.reflectionToString(child, ToStringStyle.JSON_STYLE));
		
		zkUtil.registerPersist("/hi", "hello world");
		
		zkUtil.registerEphemeral("/huang", "hi");
		zkUtil.registerEphemeralSequential("/huang2");
		
		String huang = zkUtil.get("/huang");
		System.out.println(huang);
		
		String huang2 = zkUtil.get("/huang2");
		System.out.println(huang2);
		
	}
	
	@Test
	public void getTest(){
		String value = zkUtil.get("/hi");
		System.out.println(value);
	}
	
	/**
	 * 注册机器到/mpush/allhost 目录下边
	 */
	@Test
	public void testRegister(){
		
		String path = "/"+Constants.ZK_NAME_SPACE;
		
		String prefix = Constants.ZK_REGISTER_PREFIX_NAME;
		
		List<String> hosts = zkUtil.getChildrenKeys(path);
		
		System.out.println("before register");
		
		for(int i = 0;i<hosts.size();i++){
			String value = zkUtil.get(hosts.get(i));
			System.out.println(hosts.get(i)+","+value);
		}
		
		System.out.println("start register");
		
		zkUtil.registerEphemeralSequential(path+"/"+prefix);
		
		zkUtil.registerEphemeralSequential(path+"/"+prefix);
		
		hosts = zkUtil.getChildrenKeys(path);
		
		for(int i = 0;i<hosts.size();i++){
			String value = zkUtil.get(path+"/"+hosts.get(i));
			System.out.println(hosts.get(i)+","+value);
		}
		
		System.out.println("end register");

	}
	
	@Test
	public void testLocalIp(){
		System.out.println(InetAddressUtil.getInetAddress());
		
	}
	
	@Test
	public void testRegisterIp(){
		String localIp = InetAddressUtil.getInetAddress();
		ServerApp app = new ServerApp();
		app.setIp(localIp);
		app.setPort("3000");
		zkUtil.registerPersist("/"+localIp, Jsons.toJson(app));
		String value = zkUtil.get("/"+localIp);
		System.out.println(value);
	}
	
	@Test
	public void testRemove(){
		zkUtil.remove("/example/queue");
	}
	
	@Test
	public void testAddKickOff(){
		String localIp = InetAddressUtil.getInetAddress();
		String kick = Constants.ZK_KICK;
		String ip = "10.1.10.65";
		zkUtil.registerEphemeral("/"+localIp+"/"+kick, ip);
		
	}
	

}
