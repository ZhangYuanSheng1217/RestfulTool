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

import com.github.restful.tool.view.components.editor.StyleType;
import com.github.restful.tool.view.icon.IconTypeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class AppSetting {

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

    public void initValue() {
        this.scanServicesWithLibraryDefault = false;
        this.iconTypeScheme = IconTypeManager.getInstance("default").toString();
    }

    public boolean isModified(@Nullable AppSetting setting) {
        if (setting == null) {
            return false;
        }
        return this.scanServicesWithLibraryDefault != setting.scanServicesWithLibraryDefault ||
                !this.iconTypeScheme.equals(setting.iconTypeScheme) ||
                this.enableCacheOfRestDetail != setting.enableCacheOfRestDetail ||
                !this.lightStyleType.equals(setting.lightStyleType) ||
                !this.darkStyleType.equals(setting.darkStyleType);
    }

    public void applySetting(@Nullable AppSetting setting) {
        if (setting == null) {
            return;
        }
        this.scanServicesWithLibraryDefault = setting.scanServicesWithLibraryDefault;
        this.iconTypeScheme = setting.iconTypeScheme;
        this.enableCacheOfRestDetail = setting.enableCacheOfRestDetail;
        this.lightStyleType = setting.lightStyleType;
        this.darkStyleType = setting.darkStyleType;
    }
}
