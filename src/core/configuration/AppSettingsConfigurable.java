/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: AppSettingsConfigurable
  Author:   ZhangYuanSheng
  Date:     2020/5/27 18:06
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package core.configuration;

import com.intellij.openapi.options.Configurable;
import core.view.window.AppSettingsWindow;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class AppSettingsConfigurable implements Configurable {

    private AppSettingsWindow settingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Restful Tool";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return settingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new AppSettingsWindow();
        return settingsComponent.getContent();
    }

    @Override
    public boolean isModified() {
        return AppSettingsState.getInstance().isModified(settingsComponent.getAppSetting());
    }

    @Override
    public void apply() {
        AppSettingsState.getInstance().setAppSetting(settingsComponent.getAppSetting());
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settingsComponent.setAppSetting(settings.getAppSetting());
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
