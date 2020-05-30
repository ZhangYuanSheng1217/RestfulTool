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
package core.beans;

import core.view.icon.IconTypeManager;
import core.view.icon.impl.DefaultIcon;
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
     * 图标的类型具体实现类的className
     */
    @NotNull
    public String iconTypeClass = "";

    public void initValue() {
        this.scanServicesWithLibraryDefault = false;
        this.iconTypeClass = IconTypeManager.formatClass(DefaultIcon.class);
    }

    public boolean isModified(@Nullable AppSetting setting) {
        if (setting == null) {
            return false;
        }
        return this.scanServicesWithLibraryDefault != setting.scanServicesWithLibraryDefault ||
                !this.iconTypeClass.equals(setting.iconTypeClass);
    }

    public void applySetting(@Nullable AppSetting setting) {
        if (setting == null) {
            return;
        }
        this.scanServicesWithLibraryDefault = setting.scanServicesWithLibraryDefault;
        this.iconTypeClass = setting.iconTypeClass;
    }
}
