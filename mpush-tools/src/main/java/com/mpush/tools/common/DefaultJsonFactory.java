package com.mpush.tools.common;

import com.mpush.api.spi.Spi;
import com.mpush.api.spi.common.Json;
import com.mpush.api.spi.common.JsonFactory;
import com.mpush.tools.Jsons;

@Spi
public final class DefaultJsonFactory implements JsonFactory, Json {
    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        return Jsons.fromJson(json, clazz);
    }

    @Override
    public String toJson(Object json) {
        return Jsons.toJson(json);
    }

    @Override
    public Json get() {
        return this;
    }
}