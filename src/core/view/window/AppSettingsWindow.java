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

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import core.beans.AppSetting;
import core.view.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class AppSettingsWindow {

    private final JPanel content;

    private final JBCheckBox useOldIcons;

    private final JBTextField userNameText;

    public AppSettingsWindow() {
        useOldIcons = new JBCheckBox("Use old Icons(使用旧版图标组件)");

        userNameText = new JBTextField();

        content = FormBuilder.createFormBuilder()
                .addComponent(useOldIcons, 1)
                .addComponent(getIconsPreview(), 0)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @NotNull
    private JPanel getIconsPreview() {
        JPanel iconsPreview = new JPanel(new GridLayout(1, 3));

        // 新版图标
        {
            JBLabel title = new JBLabel("默认");

            JPanel allIconsPanel = new JPanel();
            for (Icons.PreviewIcon previewIcon : Icons.getAllIcons(false)) {
                allIconsPanel.add(previewIcon);
            }
            JPanel allSelectIconsPanel = new JPanel();
            for (Icons.PreviewIcon previewIcon : Icons.getAllSelectIcons(false)) {
                allSelectIconsPanel.add(previewIcon);
            }

            iconsPreview.add(getIconsPreview(title, allIconsPanel, allSelectIconsPanel));
        }

        // 旧版图标
        {
            JBLabel title = new JBLabel("旧版");

            JPanel allOldIconsPanel = new JPanel();
            for (Icons.PreviewIcon previewIcon : Icons.getAllIcons(true)) {
                allOldIconsPanel.add(previewIcon);
            }

            iconsPreview.add(getIconsPreview(title, allOldIconsPanel, null));
        }

        return iconsPreview;
    }

    @NotNull
    private JComponent getIconsPreview(@NotNull JComponent title, @NotNull JComponent icons,
                                       @Nullable JComponent selectIcons) {

        final Color itemColor = JBColor.decode("0xe9e9e9");
        final Color bgColor = JBColor.decode("0xe9e9e9");

        JPanel iconsPreview = new JPanel(new GridLayout(3, 1));
        iconsPreview.setBackground(bgColor);
        iconsPreview.setBorder(
                JBUI.Borders.empty(0, 5)
        );
        title.setBackground(itemColor);
        iconsPreview.add(title);
        icons.setBackground(itemColor);
        iconsPreview.add(icons);
        if (selectIcons != null) {
            selectIcons.setBackground(itemColor);
            iconsPreview.add(selectIcons);
        }
        return iconsPreview;
    }

    public JPanel getContent() {
        return this.content;
    }

    public JComponent getPreferredFocusedComponent() {
        return userNameText;
    }

    @NotNull
    public AppSetting getAppSetting() {
        AppSetting setting = new AppSetting();
        setting.useOldIcons = useOldIcons.isSelected();
        return setting;
    }

    public void setAppSetting(AppSetting setting) {
        if (setting == null) {
            return;
        }
        useOldIcons.setSelected(setting.useOldIcons);
    }
}
