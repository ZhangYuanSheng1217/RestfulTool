/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: EditorStyleType
  Author:   ZhangYuanSheng
  Date:     2020/8/6 01:09
  Description: HTTP工具中的JSON语法高亮
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.beans.settings;

import cn.hutool.core.util.ReflectUtil;
import com.github.restful.tool.beans.AppSetting;
import com.github.restful.tool.view.components.editor.StyleType;
import com.github.restful.tool.view.window.options.OptionForm;
import com.github.restful.tool.view.window.options.SettingObserver;
import com.github.restful.tool.view.window.options.items.AbstractCustomComboBox;
import com.github.restful.tool.view.window.options.items.HttpToolOptions;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * HTTP工具中的JSON语法高亮
 *
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class EditorStyleType extends JPanel implements SettingObserver {

    /**
     * 亮色模式的主题
     */
    private final StyleTypeObserver lightStyleType;
    /**
     * 暗色模式的主题
     */
    private final StyleTypeObserver darkStyleType;

    public EditorStyleType() {
        super(new BorderLayout());
        lightStyleType = new StyleTypeObserver(
                "LightStyleType: ",
                "lightStyleType",
                false,
                StyleType.getLightStyles()
        );
        darkStyleType = new StyleTypeObserver(
                "DarkStyleType: ",
                "darkStyleType",
                true,
                StyleType.getDarkStyles()
        );

        JComponent lightStylePane = FormBuilder.createFormBuilder()
                .addLabeledComponent("LightStyleType: ", lightStyleType)
                .getPanel();
        JComponent darkStylePane = FormBuilder.createFormBuilder()
                .addLabeledComponent("DarkStyleType: ", darkStyleType)
                .getPanel();

        add(new JBLabel("Change JSON syntax highlighting scheme (Reopen the project to take effect)"), BorderLayout.NORTH);
        add(lightStylePane, BorderLayout.WEST);
        add(darkStylePane, BorderLayout.CENTER);

        JBEmptyBorder emptyLeft = JBUI.Borders.emptyLeft(15);
        lightStylePane.setBorder(emptyLeft);
        darkStylePane.setBorder(emptyLeft);

    }

    @Override
    public void applySetting(@NotNull AppSetting setting) {
        lightStyleType.applySetting(setting);
        darkStyleType.applySetting(setting);
    }

    @Override
    public void loadSetting(@NotNull AppSetting setting) {
        lightStyleType.loadSetting(setting);
        darkStyleType.loadSetting(setting);
    }

    @Override
    public void applyComponent(@NotNull Map<String, OptionForm> optionForms) {
        OptionForm optionForm = optionForms.get(getOptionFormName());
        optionForm.addOptionItem(this, 10);
    }

    @NotNull
    @Override
    public String getOptionFormName() {
        return HttpToolOptions.NAME;
    }

    private static class StyleTypeObserver extends AbstractCustomComboBox<StyleType> {

        private final boolean isDark;

        public StyleTypeObserver(@Nls @Nullable String title, @NotNull String settingName, boolean isDark, @NotNull StyleType[] data) {
            super(title, settingName, data);
            this.isDark = isDark;
        }

        @Override
        public void applyComponent(@NotNull Map<String, OptionForm> optionForms) {
        }

        @Override
        public void applySetting(@NotNull AppSetting setting) {
            ReflectUtil.setFieldValue(setting, getSettingName(), getSelect());
        }

        private Object getSelect() {
            StyleType styleType = (StyleType) this.getSelectedItem();
            if (styleType == null) {
                return isDark ? StyleType.DARK.name : StyleType.DEFAULT.name;
            }
            return styleType.name;
        }

        @Override
        public void loadSetting(@NotNull AppSetting setting) {
            this.setSelectedItem(StyleType.parse((String) ReflectUtil.getFieldValue(setting, getSettingName()), isDark));
        }

        @NotNull
        @Override
        public String getOptionFormName() {
            return HttpToolOptions.NAME;
        }
    }
}
