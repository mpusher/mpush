package com.mpush.api.spi;

/**
 * Created by yxx on 2016/5/18.
 *
 * @author ohun@live.cn
 */
public interface Factory<T> {
    T get();
}
