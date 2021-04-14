/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: AppSetting
  Author:   ZhangYuanSheng
  Date:     2020/5/27 18:27
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.beans.settings;

import com.github.restful.tool.beans.ContentType;
import com.github.restful.tool.beans.Key;
import com.github.restful.tool.utils.Bundle;
import com.github.restful.tool.utils.xml.converter.BaseConverter;
import com.github.restful.tool.utils.xml.converter.IntegerConverter;
import com.github.restful.tool.view.icon.IconType;
import com.github.restful.tool.view.icon.IconTypeManager;
import com.github.restful.tool.view.icon.PreviewIconType;
import com.github.restful.tool.view.window.options.Option;
import com.github.restful.tool.view.window.options.OptionForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class Settings {

    /**
     * 数据存储
     */
    @NotNull
    public Map<String, String> properties = new HashMap<>();

    /**
     * 获取所以设置项
     *
     * @return list views
     */
    @NotNull
    public static List<SettingItem> getAllSettingItems() {
        List<SettingItem> options = new CopyOnWriteArrayList<>();

        try {
            Class<? extends Settings> clazz = Settings.class;
            for (Class<?> declaredClass : clazz.getDeclaredClasses()) {
                if (!declaredClass.getSuperclass().equals(OptionForm.class)) {
                    continue;
                }
                Object instance = declaredClass.newInstance();
                if (!(instance instanceof OptionForm)) {
                    continue;
                }
                SettingItem item = new SettingItem((OptionForm) instance);
                for (Field declaredField : declaredClass.getDeclaredFields()) {
                    if (!declaredField.getType().equals(SettingKey.class)) {
                        continue;
                    }
                    Object value = declaredField.get(null);
                    if (!(value instanceof SettingKey)) {
                        continue;
                    }
                    SettingKey<?> settingKey = (SettingKey<?>) value;
                    Option option = settingKey.getOption();
                    if (!(option instanceof JComponent)) {
                        continue;
                    }
                    item.option(option);
                }
                options.add(item);
            }
        } catch (Exception ignore) {
        }

        options.sort(Comparator.comparingInt(o -> o.form.getIndex()));
        return options;
    }

    public void applySetting(@Nullable Settings setting) {
        if (setting == null) {
            return;
        }
        setting.properties.forEach(this.properties::put);
    }

    public <T> T getData(@NotNull SettingKey<T> key) {
        if (this.properties.containsKey(key.getName())) {
            return key.getConverter().fromString(this.properties.get(key.getName()));
        }
        return key.getDefaultData();
    }

    public <T> void putData(@NotNull SettingKey<T> key, @NotNull T value) {
        this.properties.put(key.getName(), key.getConverter().toString(value));
    }

    public boolean isModified(Settings changedSetting) {
        if (changedSetting == null) {
            return false;
        }
        for (Map.Entry<String, String> entry : changedSetting.properties.entrySet()) {
            if (!Objects.equals(entry.getValue(), this.properties.get(entry.getKey()))) {
                return true;
            }
        }
        return false;
    }

    public void initValue() {
        for (Key<?> key : Key.getAllKeys().values()) {
            this.properties.put(key.getName(), key.getDefaultData().toString());
        }
    }

    public static final class SettingItem {

        private final List<Option> options;
        public OptionForm form;

        public SettingItem(OptionForm form) {
            options = new CopyOnWriteArrayList<>();
            this.form = form;
        }

        public List<Option> getOptions() {
            return this.options;
        }

        @SuppressWarnings("UnusedReturnValue")
        public SettingItem option(Option option) {
            this.options.add(option);
            if (option instanceof JComponent) {
                this.form.addOptionItem((JComponent) option, option.getTopInset());
            }
            return this;
        }
    }

    public static class SystemOptionForm extends OptionForm {

        public static final SettingKey<Boolean> SCAN_WITH_LIBRARY = SettingKey.createCheckBox(
                Bundle.getString("setting.system.ScanServiceWithLibraryOnApplicationDefault"),
                false
        );

        public static final SettingKey<Boolean> EXPAND_OF_SERVICE_TREE = SettingKey.createCheckBox(
                Bundle.getString("setting.system.WhetherToExpandTheServiceTreeByDefault"),
                false
        );

        public static final SettingKey<Boolean> SHOW_CLASS_SERVICE_TREE = SettingKey.createCheckBox(
                Bundle.getString("setting.system.ShowClassServiceTreeByDefault"),
                true
        );

        public SystemOptionForm() {
            super(Bundle.getString("setting.system"), 0);
        }
    }

    public static class IconTypeOptionForm extends OptionForm {

        public static final SettingKey<IconType> ICON_TYPE_SCHEME = SettingKey.createComboBox(
                Bundle.getString("setting.iconsType.SelectIconScheme"),
                IconTypeManager.getIconTypes(),
                new BaseConverter<IconType>() {
                    @Override
                    public IconType fromString(@NotNull String value) {
                        return IconTypeManager.getInstance(value);
                    }
                },
                new JComponent[]{addIconsPreview()},
                1
        );

        public IconTypeOptionForm() {
            super(Bundle.getString("setting.iconsType"), 1);
        }

        @NotNull
        private static JPanel addIconsPreview() {
            JPanel iconsPreview = new JPanel(new GridLayout(IconTypeManager.getIconTypes().length, 1));

            for (IconType iconType : IconTypeManager.getIconTypes()) {
                iconsPreview.add(new PreviewIconType(iconType));
            }

            return iconsPreview;
        }
    }

    public static class HttpToolOptionForm extends OptionForm {

        public static final SettingKey<Integer> REDIRECT_MAX_COUNT = SettingKey.createComboBox(
                Bundle.getString("setting.httpTools.TheMaximumNumberOfRedirectsAllowed"),
                new Integer[]{0, 3, 5, 10},
                new IntegerConverter(),
                1
        );

        public static final SettingKey<ContentType> CONTENT_TYPE = SettingKey.createComboBox(
                Bundle.getString("setting.httpTools.DefaultContentType"),
                ContentType.values(),
                new BaseConverter<ContentType>() {
                    @Override
                    public ContentType fromString(@NotNull String value) {
                        return ContentType.find(value);
                    }
                },
                0
        );

        public static final SettingKey<String> CONTAINER_CONTEXT = SettingKey.createInputString(
                Bundle.getString("setting.httpTools.DefaultContextPathOfTheContainer"),
                "/",
                data -> {
                    if (data == null || data.length() < 1) {
                        return false;
                    }
                    data = data.trim();
                    final String fix = "/";
                    if (data.length() == 1) {
                        return fix.equals(data);
                    } else {
                        if (data.contains(fix + fix)) {
                            return false;
                        }
                        return data.startsWith(fix) && !data.endsWith(fix);
                    }
                }
        );

        public static final SettingKey<Integer> CONTAINER_PORT = SettingKey.createInputNumber(
                Bundle.getString("setting.httpTools.DefaultPortOfTheContainer"),
                8080,
                data -> {
                    if (data == null) {
                        return false;
                    }
                    return data >= 0 && data <= 65535;
                }
        );

        public HttpToolOptionForm() {
            super(Bundle.getString("setting.httpTools"), 2);
        }
    }
}

