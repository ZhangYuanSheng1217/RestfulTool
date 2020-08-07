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

import com.github.restful.tool.beans.Key;
import com.github.restful.tool.view.components.editor.StyleType;
import com.github.restful.tool.view.icon.IconType;
import com.github.restful.tool.view.icon.IconTypeManager;
import com.github.restful.tool.view.icon.PreviewIconType;
import com.github.restful.tool.view.window.options.OptionForm;
import com.github.restful.tool.view.window.options.Option;
import com.github.restful.tool.view.window.options.template.ComboBox;
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
public class AppSetting {

    /**
     * 数据存储
     */
    private final Map<String, Object> properties = new HashMap<>();

    /**
     * 获取所以设置项
     *
     * @return list views
     */
    @NotNull
    public static List<SettingItem> getAllSettingItems() {
        List<SettingItem> options = new CopyOnWriteArrayList<>();

        try {
            Class<? extends AppSetting> clazz = AppSetting.class;
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

    public void applySetting(@Nullable AppSetting setting) {
        if (setting == null) {
            return;
        }
        setting.properties.forEach(this.properties::put);
    }

    public <T> T getData(@NotNull Key<T> key) {
        //noinspection unchecked
        return (T) this.properties.getOrDefault(key.getName(), key.getDefaultData());
    }

    public <T> void putData(@NotNull Key<T> key, @NotNull T value) {
        this.properties.put(key.getName(), value);
    }

    public boolean isModified(AppSetting changedSetting) {
        if (changedSetting == null) {
            return false;
        }
        for (Map.Entry<String, Object> entry : changedSetting.properties.entrySet()) {
            if (!Objects.equals(entry.getValue(), this.properties.get(entry.getKey()))) {
                return true;
            }
        }
        return false;
    }

    public void initValue() {
        for (Key<?> key : Key.getAllKeys().values()) {
            this.properties.put(key.getName(), key.getDefaultData());
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

        public static final SettingKey<Boolean> SCAN_WITH_LIBRARY = SettingKey.create(
                "Scan service with library on application default (全局配置)",
                false
        );

        public static final SettingKey<Boolean> EXPAND_OF_SERVICE_TREE = SettingKey.create(
                "Whether to expand the ServiceTree by default?",
                false
        );

        public SystemOptionForm() {
            super("System", 0);
        }
    }

    public static class IconTypeOptionForm extends OptionForm {

        public static final SettingKey<IconType> ICON_TYPE_SCHEME = SettingKey.create(
                "Select Icon: ",
                IconTypeManager.getIconTypes(),
                new Option.Custom<ComboBox<IconType>>() {
                    @Override
                    public boolean showSetting(@NotNull AppSetting setting, @NotNull ComboBox<IconType> component) {
                        IconType selectItem = component.getSelectItem();
                        if (selectItem == null) {
                            return false;
                        }
                        setting.putData(component.key, IconTypeManager.getInstance(selectItem));
                        return true;
                    }

                    @Override
                    public boolean applySetting(@NotNull AppSetting setting, @NotNull ComboBox<IconType> component) {
                        IconType selectedItem = component.getSelectItem();
                        if (selectedItem == null) {
                            return false;
                        }
                        setting.putData(component.key, IconTypeManager.getInstance(selectedItem));
                        return true;
                    }
                },
                new JComponent[]{addIconsPreview()},
                1
        );

        public IconTypeOptionForm() {
            super("Icons Type", 1);
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

        public static final SettingKey<Boolean> ENABLE_CACHE_OF_REST_DETAIL = SettingKey.create(
                "Enable cache for Http Tool? (May increase memory footprint)",
                true
        );

        public static final SettingKey<Integer> REDIRECT_MAX_COUNT = SettingKey.create(
                "The maximum number of redirects allowed in the HTTP Tool: ",
                new Integer[]{0, 3, 5, 10},
                1
        );

        public static final SettingKey<StyleType> EDITOR_STYLE_TYPE_LIGHT = SettingKey.create(
                "Change JSON syntax highlighting scheme of light (Reopen the project to take effect): ",
                StyleType.getLightStyles(),
                0
        );

        public static final SettingKey<StyleType> EDITOR_STYLE_TYPE_DARK = SettingKey.create(
                "Change JSON syntax highlighting scheme of dark (Reopen the project to take effect): ",
                StyleType.getDarkStyles(),
                0
        );

        public static final SettingKey<Integer> EDITOR_STYLE_TYPE = null;

        public HttpToolOptionForm() {
            super("Http tools", 2);
        }
    }
}

