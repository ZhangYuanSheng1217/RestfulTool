package com.github.restful.tool.service.impl;

import com.github.restful.tool.service.CacheService;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class CacheServiceImpl<K, V> implements CacheService<K, V> {

    private final Project project;
    private final Map<K, V> caches;

    public CacheServiceImpl(Project project) {
        this.project = project;
        this.caches = new HashMap<>();
    }

    @Override
    public boolean clearCache(@NotNull K name) {
        if (!this.caches.containsKey(name)) {
            return false;
        }
        return this.caches.remove(name) != null;
    }

    @Override
    public long clearAll() {
        int cacheSize = this.caches.size();
        this.caches.clear();
        return cacheSize;
    }

    @Override
    public boolean hasCache(@NotNull K name) {
        return this.caches.containsKey(name);
    }

    @Override
    public V setCache(@NotNull K name, @NotNull V cache) {
        return this.caches.put(name, cache);
    }

    @Override
    public V getCache(@NotNull K name) {
        return this.caches.get(name);
    }

    @NotNull
    @Override
    public V getCache(@NotNull K name, @NotNull V defaultValue) {
        V v = this.getCache(name);
        if (v != null) {
            return v;
        }
        return defaultValue;
    }

    @NotNull
    @Override
    public Map<K, V> getCaches() {
        return Collections.unmodifiableMap(this.caches);
    }
}
