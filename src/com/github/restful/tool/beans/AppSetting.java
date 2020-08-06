/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: AppSetting
  Author:   ZhangYuanSheng
  Date:     2020/5/27 18:27
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.beans;

import com.github.restful.tool.beans.settings.*;
import com.github.restful.tool.view.components.editor.StyleType;
import com.github.restful.tool.view.icon.IconTypeManager;
import com.github.restful.tool.view.window.options.SettingObserver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class AppSetting {

    private final Map<String, String> properties = new HashMap<>();
    /**
     * 默认初始：扫描service时是否扫描lib（与项目配置分开）
     */
    public boolean scanServicesWithLibraryDefault;
    /**
     * 图标的类型具体实现类的Scheme
     */
    @NotNull
    public String iconTypeScheme = "";
    /**
     * 是否启用RestDetail的cache缓存
     */
    public boolean enableCacheOfRestDetail;
    /**
     * HTTP工具中的JSON语法高亮：亮色模式的主题
     */
    @NotNull
    public String lightStyleType = StyleType.DEFAULT.name;
    /**
     * HTTP工具中的JSON语法高亮：暗色模式的主题
     */
    @NotNull
    public String darkStyleType = StyleType.DARK.name;
    /**
     * 是否默认展开所有ServiceTree
     */
    public boolean expandOfServiceTree;
    /**
     * HTTP工具中允许的重定向的最大次数，0 则不允许
     */
    public int redirectMaxCount;

    @NotNull
    public static List<SettingObserver> getSettingObservers() {
        List<SettingObserver> observers = new ArrayList<>();
        // SystemOptions
        observers.add(new ScanServicesWithLibraryDefault());
        observers.add(new ExpandOfServiceTree());

        // IconOptions
        observers.add(new IconTypeScheme());

        // HttpToolOptions
        observers.add(new EnableCacheOfRestDetail());
        observers.add(new EditorStyleType());
        observers.add(new RedirectMaxCount());
        return observers;
    }

    public void initValue() {
        this.scanServicesWithLibraryDefault = false;
        this.iconTypeScheme = IconTypeManager.getInstance("default").toString();
        this.expandOfServiceTree = true;
        this.redirectMaxCount = 3;
    }

    @NotNull
    private Map<String, Object> getFiledValues() {
        try {
            Field[] sourceFields = this.getClass().getFields();
            Map<String, Object> values = new HashMap<>(sourceFields.length);
            for (Field sourceField : sourceFields) {
                values.put(sourceField.getName(), sourceField.get(this));
            }
            return values;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public boolean isModified(@Nullable AppSetting setting) {
        if (setting == null) {
            return false;
        }
        try {
            Map<String, Object> values = getFiledValues();
            Field[] targetFields = setting.getClass().getFields();
            for (Field targetField : targetFields) {
                if (!values.containsKey(targetField.getName())) {
                    continue;
                }
                Object obj = targetField.get(setting);
                if (obj == null) {
                    return true;
                }
                Object sourceValue = values.get(targetField.getName());
                if (!compare(sourceValue, obj)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException(
                    "[AppSetting] Check if the settings have been modified failed: " + e.getMessage()
            );
        }
    }

    private boolean compare(@NotNull Object source, Object target) {
        if (target == null) {
            return false;
        }
        if (source instanceof String && target instanceof String) {
            String sourceValue = (String) source;
            String targetValue = (String) target;
            return sourceValue.equals(targetValue);
        }
        if (source instanceof Integer && target instanceof Integer) {
            Integer sourceValue = (Integer) source;
            Integer targetValue = (Integer) target;
            return Objects.equals(sourceValue, targetValue);
        }
        if (source instanceof Boolean && target instanceof Boolean) {
            Boolean sourceValue = (Boolean) source;
            Boolean targetValue = (Boolean) target;
            return Boolean.compare(sourceValue, targetValue) == 0;
        }
        return true;
    }

    public void applySetting(@Nullable AppSetting setting) {
        if (setting == null) {
            return;
        }
        try {
            Field[] sourceFields = this.getClass().getFields();
            for (Field targetField : setting.getClass().getFields()) {
                for (Field sourceField : sourceFields) {
                    if (!targetField.getName().equals(sourceField.getName())) {
                        continue;
                    }
                    sourceField.set(this, targetField.get(setting));
                }
            }
        } catch (Exception ignore) {
        }
    }
}
