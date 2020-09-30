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

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class Bundle extends AbstractBundle {

    @NonNls
    public static final String BUNDLE = "messages.RestfulToolBundle";
    @NonNls
    public static final String BUNDLE_ZH = "messages.RestfulToolBundleZh";

    @NotNull
    private static final Bundle INSTANCE;

    static {
        final String chineseLanguagePlugin = "com.intellij.zh";
        if (PluginManager.isPluginInstalled(PluginId.getId(chineseLanguagePlugin))) {
            INSTANCE = new Bundle(BUNDLE_ZH);
        } else {
            INSTANCE = new Bundle(BUNDLE);
        }
    }

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
}
