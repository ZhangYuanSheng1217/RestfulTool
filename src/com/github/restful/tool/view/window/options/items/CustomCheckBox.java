/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: CustomCheckBox
  Author:   ZhangYuanSheng
  Date:     2020/8/5 23:53
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window.options.items;

import cn.hutool.core.util.ReflectUtil;
import com.github.restful.tool.beans.AppSetting;
import com.github.restful.tool.view.window.options.OptionForm;
import com.github.restful.tool.view.window.options.SettingObserver;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class CustomCheckBox extends JBCheckBox implements SettingObserver {

    @Nullable
    private final Integer topInset;

    private final String settingName;

    public CustomCheckBox(@Nls @Nullable String text, @NotNull String settingName) {
        this(text, settingName, null);
    }

    public CustomCheckBox(@Nls @Nullable String text, @NotNull String settingName, @Nullable Integer topInset) {
        super(text);
        this.topInset = topInset;
        this.settingName = settingName;
    }

    @Override
    public void applySetting(@NotNull AppSetting setting) {
        ReflectUtil.setFieldValue(setting, settingName, isSelected());
    }

    @Override
    public void loadSetting(@NotNull AppSetting setting) {
        setSelected((Boolean) ReflectUtil.getFieldValue(setting, settingName));
    }

    @Override
    public void applyComponent(@NotNull Map<String, OptionForm> optionForms) {
        OptionForm optionForm = optionForms.get(getOptionFormName());
        if (topInset != null) {
            optionForm.addOptionItem(this, topInset);
        } else {
            optionForm.addOptionItem(this);
        }
    }
}
