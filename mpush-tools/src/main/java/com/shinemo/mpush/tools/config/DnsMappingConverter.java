package com.shinemo.mpush.tools.config;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.aeonbits.owner.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.shinemo.mpush.tools.dns.DnsMapping;

public class DnsMappingConverter  implements Converter<Map<String, List<DnsMapping>>>{

private static final Logger log = LoggerFactory.getLogger(DnsMappingConverter.class);
	
	@Override
	public Map<String, List<DnsMapping>> convert(Method method, String input) {
		
		log.warn("method:"+method.getName()+","+input);
        Map<String, String> map = Splitter.on(';').withKeyValueSeparator('=').split(input);
        Map<String, List<DnsMapping>> result = Maps.newConcurrentMap();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
        	String value = entry.getValue();
            List<DnsMapping> dnsMappings = Lists.transform(Arrays.asList(value.split(",")), new Function<String, DnsMapping>() {
            	@Override
            	public DnsMapping apply(String ipAndPort) {
            		if(ipAndPort.contains(":")){
            			String[] temp = ipAndPort.split(":");
            			return new DnsMapping(temp[0], Ints.tryParse(temp[1]));
            		}else{
            			return new DnsMapping(ipAndPort, 80);
            		}
            	}
			});
            result.put(key, dnsMappings);
        }
        return result;
		
	}
	
}
