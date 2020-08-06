/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: AppSettingsComponent
  Author:   ZhangYuanSheng
  Date:     2020/5/27 18:06
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window.options;

import com.github.restful.tool.beans.AppSetting;
import com.github.restful.tool.view.window.options.items.HttpToolOptions;
import com.github.restful.tool.view.window.options.items.IconOptions;
import com.github.restful.tool.view.window.options.items.SystemOptions;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class AppSettingsWindow {

    public static final int VERTICAL_CLEARANCE = 30;

    private final JPanel content;

    private final List<SettingObserver> observerList;

    public AppSettingsWindow() {
        observerList = new ArrayList<>(AppSetting.getSettingObservers());

        SystemOptions systemOptions = new SystemOptions();
        IconOptions iconOptions = new IconOptions();
        HttpToolOptions httpToolOptions = new HttpToolOptions();

        Map<String, OptionForm> forms = new HashMap<>();
        forms.put(SystemOptions.NAME, systemOptions);
        forms.put(IconOptions.NAME, iconOptions);
        forms.put(HttpToolOptions.NAME, httpToolOptions);
        initSetting(forms);

        content = FormBuilder.createFormBuilder()
                .addComponent(systemOptions.getContent())
                .addComponent(iconOptions.getContent(), VERTICAL_CLEARANCE)
                .addComponent(httpToolOptions.getContent(), VERTICAL_CLEARANCE)
                .addComponentFillVertically(new JPanel(), VERTICAL_CLEARANCE)
                .getPanel();
    }

    private void initSetting(@NotNull Map<String, OptionForm> forms) {
        observerList.forEach(item -> item.applyComponent(forms));
    }

    public JPanel getContent() {
        return this.content;
    }

    public JComponent getPreferredFocusedComponent() {
        return content;
    }

    @NotNull
    public AppSetting getAppSetting() {
        AppSetting setting = new AppSetting();
        observerList.forEach(item -> item.applySetting(setting));
        return setting;
    }

    public void setAppSetting(AppSetting setting) {
        if (setting == null) {
            return;
        }
        observerList.forEach(item -> item.loadSetting(setting));
    }
}
