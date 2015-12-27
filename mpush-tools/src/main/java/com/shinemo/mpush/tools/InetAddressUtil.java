package com.shinemo.mpush.core.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InetAddressUtil {
	
	private static final Logger log = LoggerFactory.getLogger(InetAddressUtil.class);
	
	/**
	 * 获取本机ip
	 * 只获取第一块网卡绑定的ip地址
	 * @return
	 */
	public static String getInetAddress(){
		try{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress address = null;
			while(interfaces.hasMoreElements()){
				NetworkInterface ni = interfaces.nextElement();
				Enumeration<InetAddress> addresses = ni.getInetAddresses();
				while(addresses.hasMoreElements()){
					address = addresses.nextElement();
					if(!address.isLoopbackAddress()&&address.getHostAddress().indexOf(":")==-1&&address.isSiteLocalAddress()){
						return address.getHostAddress();
					}
				}
			}
			log.warn("[InetAddressUtil] getInetAddress is null");
			return "127.0.0.1";
		}catch(Throwable e){
			log.warn("[InetAddressUtil] getInetAddress exception",e);
			return "127.0.0.1";
		}
	}

}
