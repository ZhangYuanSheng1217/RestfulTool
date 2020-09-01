/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: TextInput
  Author:   ZhangYuanSheng
  Date:     2020/9/1 20:21
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window.options.template;

import com.github.restful.tool.beans.settings.SettingKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class StringInput extends BaseInput<String> {

    public StringInput(@Nullable String defaultValue, @NotNull SettingKey<String> key, Verify<String> verify) {
        super(defaultValue, key, verify, true);
        initInput();
    }

    public StringInput(@NotNull String label, @Nullable String defaultValue, @NotNull SettingKey<String> key, Integer topInset, Verify<String> verify) {
        super(label, defaultValue, key, topInset, verify, true);
        initInput();
    }

    private void initInput() {
        getInput().setColumns(16);
    }

    @Nullable
    @Override
    protected String toString(String data) {
        return data;
    }

    @Nullable
    @Override
    protected String fromString(String data) {
        return data;
    }
}
