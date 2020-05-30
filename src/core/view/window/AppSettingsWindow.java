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
package core.view.window;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import core.beans.AppSetting;
import core.view.icon.IconType;
import core.view.icon.IconTypeManager;
import core.view.icon.PreviewIconType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class AppSettingsWindow {

    private final JPanel content;

    private final JBCheckBox globalScanServiceWithLib;

    private final ComboBox<IconType> selectIconType;

    public AppSettingsWindow() {
        globalScanServiceWithLib = new JBCheckBox("Scan service with library on application default (全局配置)");

        selectIconType = new ComboBox<>(IconTypeManager.getIconTypes());

        content = FormBuilder.createFormBuilder()
                .addComponent(globalScanServiceWithLib, 0)
                .addLabeledComponent(new JBLabel("Select your icon: "), selectIconType, 5, false)
                .addComponent(getIconsPreview(), 0)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @NotNull
    private JPanel getIconsPreview() {
        JPanel iconsPreview = new JPanel(new GridLayout(IconTypeManager.getIconTypes().length, 1));

        for (IconType iconType : IconTypeManager.getIconTypes()) {
            iconsPreview.add(new PreviewIconType(iconType));
        }

        iconsPreview.setBorder(JBUI.Borders.emptyLeft(10));
        iconsPreview.setBackground(JBColor.decode("0xe9e9e9"));
        return iconsPreview;
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
        setting.iconTypeClass = IconTypeManager.formatClass(((IconType) selectIconType.getSelectedItem()).getClass());
        return setting;
    }

    public void setAppSetting(AppSetting setting) {
        if (setting == null) {
            return;
        }
        globalScanServiceWithLib.setSelected(setting.scanServicesWithLibraryDefault);
        selectIconType.setSelectedItem(IconTypeManager.getInstance(IconTypeManager.formatName(setting.iconTypeClass)));
    }
}
