package com.shinemo.mpush.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoxu.yxx on 15/8/7.
 */
public final class Jsons {
	private static final Logger LOGGER = LoggerFactory.getLogger(Jsons.class);
	public static final Gson GSON = new GsonBuilder().create();

	public static String toJson(Object bean) {

		try {
			return GSON.toJson(bean);
		} catch (Exception e) {
			LOGGER.error("Jsons.toJson ex, bean=" + bean, e);
		}
		return null;
	}

	public static <T> T fromJson(String json, Class<T> clazz) {

		try {
			return GSON.fromJson(json, clazz);
		} catch (Exception e) {
			LOGGER.error("Jsons.fromJson ex, json=" + json + ", clazz=" + clazz, e);
		}
		return null;
	}

	public static <T> T fromJson(byte[] json, Class<T> clazz) {
		return fromJson(new String(json, Constants.UTF_8), clazz);
	}

	public static <T> List<T> fromJsonToList(String json, Class<T[]> type) {
		T[] list = GSON.fromJson(json, type);
		return Arrays.asList(list);
	}

	public static <T> T fromJson(String json, Type type) {
		try {
			return GSON.fromJson(json, type);
		} catch (Exception e) {
			LOGGER.error("Jsons.fromJson ex, json=" + json + ", type=" + type, e);
		}
		return null;
	}

	public static boolean mayJson(String json) {
		if (Strings.isBlank(json))
			return false;
		if (json.charAt(0) == '{' && json.charAt(json.length() - 1) == '}')
			return true;
		if (json.charAt(0) == '[' && json.charAt(json.length() - 1) == ']')
			return true;
		return false;
	}

	public static String toJson(Map<String, String> map) {
		if (map == null || map.isEmpty())
			return "{}";
		StringBuilder sb = new StringBuilder(64 * map.size());
		sb.append('{');
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		if (it.hasNext()) {
			append(it.next(), sb);
		}
		while (it.hasNext()) {
			sb.append(',');
			append(it.next(), sb);
		}
		sb.append('}');
		return sb.toString();
	}

	private static void append(Map.Entry<String, String> entry, StringBuilder sb) {
		String key = entry.getKey(), value = entry.getValue();
		if (value == null)
			value = Strings.EMPTY;
		sb.append('"').append(key).append('"');
		sb.append(':');
		sb.append('"').append(value).append('"');
	}

	public static void main(String[] args) {
		String test = "test";
		String ret = Jsons.toJson(test);
		String ret2 = Jsons.fromJson(ret, String.class);
		System.out.println(ret);
		System.out.println(ret2);
	}
}
