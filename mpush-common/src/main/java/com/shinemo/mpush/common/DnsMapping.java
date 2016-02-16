package com.shinemo.mpush.common;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.shinemo.mpush.tools.config.ConfigCenter;

import java.util.List;
import java.util.Map;

/**
 * Created by ohun on 2016/2/16.
 */
public final class DnsMapping {
    private final ArrayListMultimap<String, String> mappings = ArrayListMultimap.create();

    public DnsMapping() {
        String dnsString = ConfigCenter.holder.dnsMapping();
        if (Strings.isNullOrEmpty(dnsString)) return;

        Map<String, String> map = Splitter.on(';').withKeyValueSeparator('=').split(dnsString);
        Splitter vsp = Splitter.on(',');
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String value = entry.getValue();
            if (Strings.isNullOrEmpty(value)) continue;
            Iterable<String> it = vsp.split(entry.getValue());
            mappings.putAll(entry.getKey(), it);
        }
    }

    public String translate(String origin) {
        if (mappings.isEmpty()) return null;
        List<String> list = mappings.get(origin);
        if (list == null || list.isEmpty()) return null;
        int L = list.size();
        if (L == 1) return list.get(0);
        return list.get((int) (Math.random() * L % L));
    }
}
