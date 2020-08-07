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
import com.github.restful.tool.utils.SystemUtil;
import com.github.restful.tool.view.window.options.Option;
import com.github.restful.tool.configuration.AppSettingsState;
import com.github.restful.tool.view.window.options.template.CheckBox;
import com.github.restful.tool.view.window.options.template.ComboBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class SettingKey<T> extends Key<T> {

    private Option option;

    private SettingKey(String name, T defaultData) {
        super(name, defaultData);
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static SettingKey<Boolean> create(@NotNull String name, @NotNull Boolean defaultData) {
        SettingKey<Boolean> settingKey = new SettingKey<>(name, defaultData);
        settingKey.option = new CheckBox(settingKey, null);
        return settingKey;
    }

    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <T> SettingKey<T> create(@NotNull String name, @NotNull final T[] dataArray, int... defaultDataIndex) {
        SettingKey<T> settingKey = new SettingKey<>(name, dataArray[SystemUtil.Array.getLegalSubscript(
                dataArray,
                defaultDataIndex != null && defaultDataIndex.length > 0 ? defaultDataIndex[0] : null
        )]);
        settingKey.option = new ComboBox<>(dataArray, settingKey);
        return settingKey;
    }

    @NotNull
    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static <T> SettingKey<T> create(@NotNull String name, @NotNull final T[] dataArray, @NotNull Option.Custom<ComboBox<T>> other, int... defaultDataIndex) {
        SettingKey<T> settingKey = new SettingKey<>(name, dataArray[SystemUtil.Array.getLegalSubscript(
                dataArray,
                defaultDataIndex != null && defaultDataIndex.length > 0 ? defaultDataIndex[0] : null
        )]);
        settingKey.option = new ComboBox<T>(dataArray, settingKey) {
            @Override
            public void showSetting(@NotNull AppSetting setting) {
                if (!other.showSetting(setting, this)) {
                    super.showSetting(setting);
                }
            }

            @Override
            public void applySetting(@NotNull AppSetting setting) {
                if (!other.applySetting(setting, this)) {
                    super.applySetting(setting);
                }
            }

            @Nullable
            @Override
            public Integer getTopInset() {
                Integer topInset = other.getTopInset();
                return topInset == null ? super.getTopInset() : topInset;
            }
        };
        return settingKey;
    }

    @NotNull
    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    public static <T> SettingKey<T> create(@NotNull String name, @NotNull final T[] dataArray, @NotNull Option.Custom<ComboBox<T>> other, @NotNull JComponent[] components, int... defaultDataIndex) {
        SettingKey<T> settingKey = new SettingKey<>(name, dataArray[SystemUtil.Array.getLegalSubscript(
                dataArray,
                defaultDataIndex != null && defaultDataIndex.length > 0 ? defaultDataIndex[0] : null
        )]);
        settingKey.option = new ComboBox<T>(dataArray, settingKey, null, components) {
            @Override
            public void showSetting(@NotNull AppSetting setting) {
                if (!other.showSetting(setting, this)) {
                    super.showSetting(setting);
                }
            }

            @Override
            public void applySetting(@NotNull AppSetting setting) {
                if (!other.applySetting(setting, this)) {
                    super.applySetting(setting);
                }
            }

            @Nullable
            @Override
            public Integer getTopInset() {
                Integer topInset = other.getTopInset();
                return topInset == null ? super.getTopInset() : topInset;
            }
        };
        return settingKey;
    }

    @NotNull
    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static <T> SettingKey<T> create(@NotNull String name, @NotNull final T[] dataArray, @NotNull JComponent[] components, int... defaultDataIndex) {
        SettingKey<T> settingKey = new SettingKey<>(name, dataArray[SystemUtil.Array.getLegalSubscript(
                dataArray,
                defaultDataIndex != null && defaultDataIndex.length > 0 ? defaultDataIndex[0] : null
        )]);
        settingKey.option = new ComboBox<>(dataArray, settingKey, null, components);
        return settingKey;
    }

    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <T> SettingKey<T> create(@NotNull String name, @NotNull T defaultData, @NotNull Option option) {
        SettingKey<T> settingKey = new SettingKey<>(name, defaultData);
        settingKey.option = option;
        return settingKey;
    }

    public T getData() {
        AppSetting appSetting = AppSettingsState.getInstance().getAppSetting();
        return appSetting.getData(this);
    }

    public void setData(@NotNull T data) {
        AppSetting appSetting = AppSettingsState.getInstance().getAppSetting();
        appSetting.putData(this, data);
    }

    @Nullable
    public Option getOption() {
        return option;
    }

    public void setOption(@Nullable Option option) {
        this.option = option;
    }
}
