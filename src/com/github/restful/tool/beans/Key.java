/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: Key
  Author:   ZhangYuanSheng
  Date:     2020/8/6 16:24
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.beans;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.IntObjectMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class Key<T> {

    private static final AtomicInteger OUR_KEYS_COUNTER = new AtomicInteger();
    private static final IntObjectMap<Key<?>> ALL_KEYS = ContainerUtil.createConcurrentIntObjectWeakValueMap();

    private final int index = OUR_KEYS_COUNTER.getAndIncrement();

    private final String name;
    private final T defaultData;

    protected Key(String name, T defaultData) {
        this.name = name;
        this.defaultData = defaultData;
        ALL_KEYS.put(getIndex(), this);
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static <T> Key<T> create(@NotNull String name, @NotNull T defaultData) {
        return new Key<>(name, defaultData);
    }

    @Nullable
    public static Key<?> findKeyByName(@NotNull String name) {
        for (IntObjectMap.Entry<Key<?>> key : ALL_KEYS.entrySet()) {
            if (name.equals(key.getValue().name)) {
                return key.getValue();
            }
        }
        return null;
    }

    @NotNull
    public static IntObjectMap<Key<?>> getAllKeys() {
        return Key.ALL_KEYS;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public T getDefaultData() {
        return defaultData;
    }

    @Override
    public int hashCode() {
        return this.index;
    }

    @Override
    public final boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public int getIndex() {
        return index;
    }
}
