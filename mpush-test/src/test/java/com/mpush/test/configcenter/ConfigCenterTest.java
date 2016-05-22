package com.mpush.test.configcenter;

import com.mpush.tools.config.CC;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class ConfigCenterTest {
    @Before
    public void setUp() throws Exception {
        // ConfigManager.I.getLocalIp();
    }

    @Test
    public void testKey() {
        //String t = ConfigKey.app_env.getString();
        System.out.println(CC.mp.security.aes_key_length);
        System.out.println(CC.mp.redis.cluster_group);
    }

    @Test
    public void testLoad() {
        Map<String, String> map = new HashMap<>();
        CC.cfg.entrySet().forEach(e -> print(e.getKey(), e.getValue(), map));
        List<String> list = new ArrayList<>(map.values());
        Collections.sort(list);
        list.forEach(s -> System.out.println(s.substring(s.indexOf(".") + 1) + ","));
    }

    public static void print(String s, ConfigValue configValue, Map<String, String> map) {
        if (s.startsWith("mp") && !s.endsWith("\"")) {
            String[] keys = s.split("\\.");
            if (keys.length >= 4) return;
            for (int i = keys.length - 1; i > 0; i--) {
                String key = keys[i];
                String value = map.get(key);
                if (value != null) continue;
                String p = keys[i - 1];
                map.put(key, p + "." + key.replace('-', '_') + "(" + p + ")");
            }
        }
    }

    @Test
    public void testLoad2() {
        Map<String, String> map = new HashMap<>();
        System.out.println("public interface mp {");
        System.out.printf("  Config cfg = ConfigManager.I.mp().toConfig();%n%n");
        CC.mp.cfg.root().forEach((s, configValue) -> print2(s, configValue, "mp", 1));
        System.out.println("}");
    }

    public static void print2(String s, ConfigValue configValue, String p, int level) {
        int l = level + 1;
        switch (configValue.valueType()) {
            case OBJECT:
                printTab(level);
                System.out.printf("interface %s {%n", s.replace('-', '_'));
                printTab(level);
                System.out.printf("    Config cfg = %s.cfg.getObject(\"%s\").toConfig();%n%n", p, s);
                ((ConfigObject) configValue).forEach((s1, configValue2) -> print2(s1, configValue2, s, l));
                printTab(level);
                System.out.printf("}%n%n");
                break;
            case STRING:
                printTab(level);
                System.out.printf("  String %s = cfg.getString(\"%s\");%n%n", s.replace('-', '_'), s);
                break;
            case NUMBER:
                printTab(level);
                System.out.printf("  Number %s = cfg.getNumber(\"%s\");%n%n", s.replace('-', '_'), s);
                break;
            case BOOLEAN:
                printTab(level);
                System.out.printf("  Boolean %s = cfg.getBoolean(\"%s\");%n%n", s.replace('-', '_'), s);
                break;
            case LIST:
                printTab(level);
                System.out.printf("  List<Object> %s = cfg.getList(\"%s\").unwrapped();%n%n", s.replace('-', '_'), s);
                break;
        }
    }

    private static void printTab(int l) {
        while (l-- > 0) {
            System.out.print("  ");
        }
    }
}
