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
package core.view.window.options;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
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

    public static final int VERTICAL_CLEARANCE = 30;

    private final JPanel content;

    private final JBCheckBox globalScanServiceWithLib;

    private final ComboBox<IconType> selectIconType;

    public AppSettingsWindow() {
        globalScanServiceWithLib = new JBCheckBox("Scan service with library on application default (全局配置)");

        selectIconType = new ComboBox<>(IconTypeManager.getIconTypes());

        content = FormBuilder.createFormBuilder()
                .addComponent(new SystemOptions().getContent())
                .addComponent(new IconOptions().getContent(), VERTICAL_CLEARANCE)
                .addComponent(new OptionForm("Test Ignore").getContent(), VERTICAL_CLEARANCE)
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
}
