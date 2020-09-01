/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: Key
  Author:   ZhangYuanSheng
  Date:     2020/8/6 16:24
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.beans.settings;

import com.github.restful.tool.beans.Key;
import com.github.restful.tool.configuration.RestfulSetting;
import com.github.restful.tool.utils.SystemUtil;
import com.github.restful.tool.utils.xml.converter.BooleanConverter;
import com.github.restful.tool.utils.xml.converter.IntegerConverter;
import com.github.restful.tool.utils.xml.converter.StringConverter;
import com.github.restful.tool.view.window.options.Option;
import com.github.restful.tool.view.window.options.template.*;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class SettingKey<T> extends Key<T> {

    public static final StringConverter STRING_CONVERTER = new StringConverter();
    public static final BooleanConverter BOOLEAN_CONVERTER = new BooleanConverter();
    public static final IntegerConverter INTEGER_CONVERTER = new IntegerConverter();

    private final Converter<T> converter;
    private Option option;

    private SettingKey(String name, T defaultData, @NotNull Converter<T> converter) {
        super(name, defaultData);
        this.converter = converter;
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static SettingKey<Boolean> createCheckBox(@NotNull String name, @NotNull Boolean defaultData) {
        SettingKey<Boolean> settingKey = new SettingKey<>(name, defaultData, BOOLEAN_CONVERTER);
        settingKey.option = new CheckBox(settingKey, null);
        return settingKey;
    }

    @NotNull
    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static <T> SettingKey<T> createComboBox(@NotNull String name, @NotNull final T[] dataArray, @NotNull Converter<T> converter, int... defaultDataIndex) {
        SettingKey<T> settingKey = new SettingKey<>(name, dataArray[SystemUtil.Array.getLegalSubscript(
                dataArray,
                defaultDataIndex != null && defaultDataIndex.length > 0 ? defaultDataIndex[0] : null
        )], converter);
        settingKey.option = new ComboBox<>(dataArray, settingKey);
        return settingKey;
    }

    @NotNull
    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    public static <T> SettingKey<T> createComboBox(@NotNull String name, @NotNull final T[] dataArray, @NotNull Converter<T> converter, @NotNull JComponent[] components, int... defaultDataIndex) {
        SettingKey<T> settingKey = new SettingKey<>(name, dataArray[SystemUtil.Array.getLegalSubscript(
                dataArray,
                defaultDataIndex != null && defaultDataIndex.length > 0 ? defaultDataIndex[0] : null
        )], converter);
        settingKey.option = new ComboBox<>(dataArray, settingKey, null, components);
        return settingKey;
    }

    @NotNull
    public static SettingKey<String> createInputString(@NotNull String name, @NotNull String defaultData, @NotNull BaseInput.Verify<String> verify) {
        SettingKey<String> settingKey = new SettingKey<>(name, defaultData, STRING_CONVERTER);
        settingKey.option = new StringInput(defaultData, settingKey, verify);
        return settingKey;
    }

    @NotNull
    public static SettingKey<Integer> createInputNumber(@NotNull String name, @NotNull Integer defaultData, @NotNull BaseInput.Verify<Integer> verify) {
        SettingKey<Integer> settingKey = new SettingKey<>(name, defaultData, INTEGER_CONVERTER);
        settingKey.option = new IntegerInput(defaultData, settingKey, verify);
        return settingKey;
    }

    public T getData() {
        Settings appSetting = RestfulSetting.getInstance().getAppSetting();
        return appSetting.getData(this);
    }

    public void setData(@NotNull T data) {
        Settings appSetting = RestfulSetting.getInstance().getAppSetting();
        appSetting.putData(this, data);
    }

    @Nullable
    public Option getOption() {
        return option;
    }

    public void setOption(@Nullable Option option) {
        this.option = option;
    }

    @NotNull
    public Converter<T> getConverter() {
        return converter;
    }
}
