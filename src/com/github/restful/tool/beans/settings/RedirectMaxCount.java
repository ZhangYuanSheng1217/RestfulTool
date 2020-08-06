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
import com.github.restful.tool.view.window.options.items.AbstractCustomComboBox;
import com.github.restful.tool.view.window.options.items.HttpToolOptions;
import org.jetbrains.annotations.NotNull;

/**
 * HTTP工具中允许的重定向的最大次数，0 则不允许
 *
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RedirectMaxCount extends AbstractCustomComboBox<Integer> {

    public RedirectMaxCount() {
        super(
                "The maximum number of redirects allowed in the HTTP Tool: ",
                "redirectMaxCount",
                10,
                new Integer[]{0, 3, 5, 10}
        );
    }

    @Override
    public void applySetting(@NotNull AppSetting setting) {
        ReflectUtil.setFieldValue(setting, getSettingName(), getSelectedItem());
    }

    @Override
    public void loadSetting(@NotNull AppSetting setting) {
        this.setSelectedItem(ReflectUtil.getFieldValue(setting, getSettingName()));
    }

    @NotNull
    @Override
    public String getOptionFormName() {
        return HttpToolOptions.NAME;
    }
}
