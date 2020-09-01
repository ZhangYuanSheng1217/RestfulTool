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

import com.github.restful.tool.beans.settings.Settings;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestfulSettingWindow {

    public static final int VERTICAL_CLEARANCE = 30;

    private final JPanel content;
    private final List<Option> optionList;

    public RestfulSettingWindow() {
        optionList = new ArrayList<>();
        FormBuilder builder = FormBuilder.createFormBuilder();

        List<Settings.SettingItem> allSettingItems = Settings.getAllSettingItems();
        for (int i = 0; i < allSettingItems.size(); i++) {
            Settings.SettingItem item = allSettingItems.get(i);
            if (i == 0) {
                builder.addComponent(item.form.getContent());
            } else {
                builder.addComponent(item.form.getContent(), VERTICAL_CLEARANCE);
            }
            optionList.addAll(item.getOptions());
        }

        content = builder.addComponentFillVertically(new JPanel(), VERTICAL_CLEARANCE).getPanel();
    }

    public JPanel getContent() {
        return this.content;
    }

    public JComponent getPreferredFocusedComponent() {
        return content;
    }

    @NotNull
    public Settings getAppSetting() {
        Settings setting = new Settings();
        optionList.forEach(item -> item.applySetting(setting));
        return setting;
    }

    public void setAppSetting(Settings setting) {
        if (setting == null) {
            return;
        }
        optionList.forEach(item -> item.showSetting(setting));
    }
}
