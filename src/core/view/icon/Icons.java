/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: Icons
  Author:   ZhangYuanSheng
  Date:     2020/5/6 10:39
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package core.view.icon;

import com.intellij.ui.IconManager;
import core.beans.HttpMethod;
import core.configuration.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class Icons {

    public static final Icon SERVICE = load("/icons/service.png");

    @NotNull
    public static Icon load(@NotNull String path) {
        return IconManager.getInstance().getIcon(path, Icons.class);
    }

    /**
     * 获取方法对应的图标
     *
     * @param method 请求类型
     * @return icon
     */
    @NotNull
    public static Icon getMethodIcon(HttpMethod method) {
        return getMethodIcon(method, false);
    }

    public static Icon getMethodIcon(HttpMethod method, boolean selected) {
        String iconTypeClass = AppSettingsState.getInstance().getAppSetting().iconTypeClass;
        IconType iconType = IconTypeManager.getInstance(iconTypeClass);
        if (selected) {
            return iconType.getSelectIcon(method);
        }
        return iconType.getDefaultIcon(method);
    }
}
