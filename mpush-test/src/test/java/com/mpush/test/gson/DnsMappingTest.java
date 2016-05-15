package com.mpush.test.gson;

import java.util.Map;

import org.junit.Test;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;

public class DnsMappingTest {
	
	@Test
	public void test(){
		
		ArrayListMultimap<String, String> mappings = ArrayListMultimap.create();
		
		String dnsString = "111.1.57.148=127.0.0.1,127.0.0.2;120.27.196.68=127.0.0.1;120.27.198.172=127.0.0.1";
        if (Strings.isNullOrEmpty(dnsString)) return;

        Map<String, String> map = Splitter.on(';').withKeyValueSeparator('=').split(dnsString);
        Splitter vsp = Splitter.on(',');
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String value = entry.getValue();
            if (Strings.isNullOrEmpty(value)) continue;
            Iterable<String> it = vsp.split(entry.getValue());
            mappings.putAll(entry.getKey(), it);
        }
        
        System.out.println(mappings);
		
	}

}
