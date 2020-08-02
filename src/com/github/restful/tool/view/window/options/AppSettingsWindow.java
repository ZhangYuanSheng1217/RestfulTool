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

import com.github.restful.tool.beans.AppSetting;
import com.github.restful.tool.view.icon.IconType;
import com.github.restful.tool.view.icon.PreviewIconType;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import com.github.restful.tool.view.components.editor.StyleType;
import com.github.restful.tool.view.icon.IconTypeManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class AppSettingsWindow {

    public static final int VERTICAL_CLEARANCE = 30;

    private final JPanel content;

    private final JBCheckBox globalScanServiceWithLib;

    private final ComboBox<IconType> selectIconType;

    private final JBCheckBox enableCacheOfRestDetail;

    private final ComboBox<StyleType> lightStyleType;
    private final ComboBox<StyleType> darkStyleType;

    public AppSettingsWindow() {
        globalScanServiceWithLib = new JBCheckBox("Scan service with library on application default (全局配置)");

        selectIconType = new ComboBox<>(IconTypeManager.getIconTypes());

        enableCacheOfRestDetail = new JBCheckBox("Enable cache for Http Tool? (May increase memory footprint)");

        lightStyleType = new ComboBox<>(StyleType.getLightStyles());
        darkStyleType = new ComboBox<>(StyleType.getDarkStyles());

        content = FormBuilder.createFormBuilder()
                .addComponent(new SystemOptions().getContent())
                .addComponent(new IconOptions().getContent(), VERTICAL_CLEARANCE)
                .addComponent(new HttpToolOptions().getContent(), VERTICAL_CLEARANCE)
                .addComponentFillVertically(new JPanel(), VERTICAL_CLEARANCE)
                .getPanel();
    }

    public JPanel getContent() {
        return this.content;
    }

    public JComponent getPreferredFocusedComponent() {
        return globalScanServiceWithLib;
    }

    @NotNull
    public AppSetting getAppSetting() {
        AppSetting setting = new AppSetting();
        setting.scanServicesWithLibraryDefault = globalScanServiceWithLib.isSelected();
        //noinspection ConstantConditions
        setting.iconTypeScheme = IconTypeManager.getInstance(selectIconType.getSelectedItem()).toString();
        setting.enableCacheOfRestDetail = enableCacheOfRestDetail.isSelected();

        StyleType lightSelected = (StyleType) lightStyleType.getSelectedItem();
        StyleType darkSelected = (StyleType) darkStyleType.getSelectedItem();
        setting.lightStyleType = lightSelected == null ? StyleType.DEFAULT.name : lightSelected.name;
        setting.darkStyleType = darkSelected == null ? StyleType.DARK.name : darkSelected.name;

        return setting;
    }

    public void setAppSetting(AppSetting setting) {
        if (setting == null) {
            return;
        }
        globalScanServiceWithLib.setSelected(setting.scanServicesWithLibraryDefault);
        selectIconType.setSelectedItem(IconTypeManager.getInstance(setting.iconTypeScheme));
        enableCacheOfRestDetail.setSelected(setting.enableCacheOfRestDetail);

        lightStyleType.setSelectedItem(StyleType.parse(setting.lightStyleType, false));
        darkStyleType.setSelectedItem(StyleType.parse(setting.darkStyleType, true));
    }

    private class SystemOptions extends OptionForm {

        public SystemOptions() {
            super("System");

            this.addOptionItem(globalScanServiceWithLib);
        }
    }

    private class IconOptions extends OptionForm {

        public IconOptions() {
            super("Icons");

            this.addLabeledOptionItem("Select Icon: ", selectIconType);
            this.addOptionItem(addIconsPreview());
        }

        @NotNull
        private JPanel addIconsPreview() {
            JPanel iconsPreview = new JPanel(new GridLayout(IconTypeManager.getIconTypes().length, 1));

            for (IconType iconType : IconTypeManager.getIconTypes()) {
                iconsPreview.add(new PreviewIconType(iconType));
            }

            return iconsPreview;
        }
    }

    private class HttpToolOptions extends OptionForm {

        public HttpToolOptions() {
            super("Http Tool");

            this.addOptionItem(enableCacheOfRestDetail);
            this.addOptionItem(getStyleTypeView(), 10);
        }

        @NotNull
        private JComponent getStyleTypeView() {
            JPanel panel = new JPanel(new BorderLayout());

            JComponent lightStylePane = FormBuilder.createFormBuilder()
                    .addLabeledComponent("LightStyleType: ", AppSettingsWindow.this.lightStyleType)
                    .getPanel();
            JComponent darkStylePane = FormBuilder.createFormBuilder()
                    .addLabeledComponent("DarkStyleType: ", darkStyleType)
                    .getPanel();

            panel.add(new JBLabel("Change JSON syntax highlighting scheme (Reopen the project to take effect)"), BorderLayout.NORTH);
            panel.add(lightStylePane, BorderLayout.WEST);
            panel.add(darkStylePane, BorderLayout.CENTER);

            JBEmptyBorder emptyLeft = JBUI.Borders.emptyLeft(15);
            lightStylePane.setBorder(emptyLeft);
            darkStylePane.setBorder(emptyLeft);
            return panel;
        }
    }
}
