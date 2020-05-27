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
package core.beans;

import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class AppSetting {

    public boolean useOldIcons;

    public void initValue() {
        this.useOldIcons = false;
    }

    public boolean isModified(@Nullable AppSetting setting) {
        if (setting == null) {
            return false;
        }
        return this.useOldIcons != setting.useOldIcons;
    }

    public void applySetting(@Nullable AppSetting setting) {
        if (setting == null) {
            return;
        }
        this.useOldIcons = setting.useOldIcons;
    }
}
