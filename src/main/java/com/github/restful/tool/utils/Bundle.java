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

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
@SuppressWarnings("MissingRecentApi")
public class Bundle extends DynamicBundle {

    @NonNls
    public static final String BUNDLE = "messages.RestfulToolBundle";

    private static final Bundle INSTANCE = new Bundle();

    private Bundle() {
        super(BUNDLE);
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
