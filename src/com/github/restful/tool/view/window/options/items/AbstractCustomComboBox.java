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

import com.github.restful.tool.view.window.options.OptionForm;
import com.github.restful.tool.view.window.options.SettingObserver;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public abstract class AbstractCustomComboBox<E> extends ComboBox<E> implements SettingObserver {

    @Nullable
    private final Integer topInset;

    private final String title;

    private final String settingName;

    public AbstractCustomComboBox(@Nls @Nullable String title, @NotNull String settingName, @NotNull E[] data) {
        this(title, settingName, null, data);
    }

    public AbstractCustomComboBox(@Nls @Nullable String title, @NotNull String settingName, @Nullable Integer topInset, @NotNull E[] data) {
        super(data);
        this.topInset = topInset;
        this.title = title;
        this.settingName = settingName;
    }

    @NotNull
    public final String getSettingName() {
        return settingName;
    }

    @Override
    public void applyComponent(@NotNull Map<String, OptionForm> optionForms) {
        OptionForm optionForm = optionForms.get(getOptionFormName());
        if (topInset != null) {
            optionForm.addLabeledOptionItem(title, this, topInset);
        } else {
            optionForm.addLabeledOptionItem(title, this);
        }
    }
}
