/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: Bundle
  Author:   ZhangYuanSheng
  Date:     2020/9/4 23:15
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils;

import com.intellij.AbstractBundle;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class Bundle extends AbstractBundle {

    @NonNls
    public static final String BUNDLE = "messages.RestfulToolBundle";

    @NotNull
    private static final Bundle INSTANCE = new Bundle(BUNDLE);

    private Bundle(@NonNls String resource) {
        super(resource);
    }

    @Nls
    @NotNull
    public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    @Nls
    @NotNull
    public static String getString(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return message(key, params);
    }

    @Override
    protected ResourceBundle findBundle(@NotNull @NonNls String pathToBundle,
                                        @NotNull ClassLoader loader,
                                        @NotNull ResourceBundle.Control control) {
        final String chineseLanguagePlugin = "com.intellij.zh";
        if (!PluginManager.isPluginInstalled(PluginId.getId(chineseLanguagePlugin))) {
            // 未安装 IDE中文语言包 插件则使用默认
            return ResourceBundle.getBundle(pathToBundle, Locale.ROOT, loader, control);
        }
        return ResourceBundle.getBundle(pathToBundle, Locale.getDefault(), loader, control);
    }
}
