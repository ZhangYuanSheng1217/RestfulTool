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

import com.github.restful.tool.beans.settings.Settings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
@State(name = "RestfulSetting", storages = @Storage(AppSetting.IDE))
public class AppSetting implements PersistentStateComponent<AppSetting> {

    public static final String IDE = "RestfulToolSetting.xml";

    private final Settings setting;

    public AppSetting() {
        this.setting = new Settings();
        this.setting.initValue();
    }

    public static AppSetting getInstance() {
        return ApplicationManager.getApplication().getService(AppSetting.class);
    }

    public boolean isModified(Settings changedSetting) {
        if (changedSetting == null) {
            return false;
        }
        return this.setting.isModified(changedSetting);
    }

    @Nullable
    @Override
    public AppSetting getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSetting state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @NotNull
    public Settings getAppSetting() {
        return this.setting;
    }

    public void setAppSetting(Settings setting) {
        this.setting.applySetting(setting);
    }
}
