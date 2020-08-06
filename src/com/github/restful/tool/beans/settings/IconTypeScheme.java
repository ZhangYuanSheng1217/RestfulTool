/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: IconTypeScheme
  Author:   ZhangYuanSheng
  Date:     2020/8/6 00:44
  Description: 图标的类型具体实现类的Scheme
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.beans.settings;

import cn.hutool.core.util.ReflectUtil;
import com.github.restful.tool.beans.AppSetting;
import com.github.restful.tool.view.icon.IconType;
import com.github.restful.tool.view.icon.IconTypeManager;
import com.github.restful.tool.view.icon.PreviewIconType;
import com.github.restful.tool.view.window.options.OptionForm;
import com.github.restful.tool.view.window.options.items.AbstractCustomComboBox;
import com.github.restful.tool.view.window.options.items.IconOptions;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * 图标的类型具体实现类的Scheme
 *
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class IconTypeScheme extends AbstractCustomComboBox<IconType> {

    public IconTypeScheme() {
        super(
                "Select Icon: ",
                "iconTypeScheme",
                IconTypeManager.getIconTypes()
        );
    }

    @Override
    public void applySetting(@NotNull AppSetting setting) {
        //noinspection ConstantConditions
        ReflectUtil.setFieldValue(setting, getSettingName(), IconTypeManager.getInstance(this.getSelectedItem()));
    }

    @Override
    public void loadSetting(@NotNull AppSetting setting) {
        this.setSelectedItem(IconTypeManager.getInstance(ReflectUtil.getFieldValue(setting, getSettingName())));
    }

    @Override
    public void applyComponent(@NotNull Map<String, OptionForm> optionForms) {
        super.applyComponent(optionForms);
        OptionForm optionForm = optionForms.get(getOptionFormName());
        optionForm.addOptionItem(addIconsPreview());
    }

    @NotNull
    private JPanel addIconsPreview() {
        JPanel iconsPreview = new JPanel(new GridLayout(IconTypeManager.getIconTypes().length, 1));

        for (IconType iconType : IconTypeManager.getIconTypes()) {
            iconsPreview.add(new PreviewIconType(iconType));
        }

        return iconsPreview;
    }

    @NotNull
    @Override
    public String getOptionFormName() {
        return IconOptions.NAME;
    }
}
