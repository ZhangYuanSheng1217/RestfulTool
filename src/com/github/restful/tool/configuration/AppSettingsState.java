/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: AppSettingsState
  Author:   ZhangYuanSheng
  Date:     2020/5/27 18:08
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.configuration;

import com.github.restful.tool.beans.settings.AppSetting;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
@State(
        name = "com.github.restful.tool.configuration.AppSettingsState",
        storages = {
                @Storage("SdkSettingsPlugin.xml")
        }
)
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

    private final AppSetting setting;

    public AppSettingsState() {
        this.setting = new AppSetting();
        this.setting.initValue();
    }

    public static AppSettingsState getInstance() {
        return ServiceManager.getService(AppSettingsState.class);
    }

    public boolean isModified(AppSetting changedSetting) {
        if (changedSetting == null) {
            return false;
        }
        return this.setting.isModified(changedSetting);
    }

    @Nullable
    @Override
    public AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @NotNull
    public AppSetting getAppSetting() {
        return this.setting;
    }

    public void setAppSetting(AppSetting setting) {
        this.setting.applySetting(setting);
    }
}
