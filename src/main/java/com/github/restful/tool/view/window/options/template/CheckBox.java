/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: CheckBox
  Author:   ZhangYuanSheng
  Date:     2020/8/6 17:31
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window.options.template;

import com.github.restful.tool.beans.settings.SettingKey;
import com.github.restful.tool.beans.settings.Settings;
import com.github.restful.tool.view.window.options.Option;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class CheckBox extends JBCheckBox implements Option {

    public final SettingKey<Boolean> key;
    private final Integer topInset;

    public CheckBox(@NotNull SettingKey<Boolean> key, @Nullable Integer topInset) {
        this(key.getName(), key, topInset);
    }

    public CheckBox(@Nls @NotNull String title, @NotNull SettingKey<Boolean> key) {
        this(title, key, null);
    }

    public CheckBox(@Nls @NotNull String title, @NotNull SettingKey<Boolean> key, @Nullable Integer topInset) {
        super(title);
        this.key = key;
        this.topInset = topInset;
    }

    @Override
    public void showSetting(@NotNull Settings setting) {
        this.setSelected(setting.getData(this.key));
    }

    @Override
    public void applySetting(@NotNull Settings setting) {
        setting.putData(this.key, this.isSelected());
    }

    @Nullable
    @Override
    public Integer getTopInset() {
        return this.topInset;
    }
}
