/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: StyleType
  Author:   ZhangYuanSheng
  Date:     2020/7/13 20:26
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.components.editor;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public enum StyleType {

    /**
     * dark
     */
    DARK("dark", true),

    /**
     * druid
     */
    DRUID("druid", true),

    /**
     * monokai
     */
    MONOKAI("monokai", true),

    /**
     * default
     */
    DEFAULT("default", false),

    /**
     * eclipse
     */
    ECLIPSE("eclipse", false),

    /**
     * idea
     */
    IDEA("idea", false),

    /**
     * vs
     */
    VS("vs", false);

    private static final String BASE_PATH = "/org/fife/ui/rsyntaxtextarea/themes/";

    public final String name;
    public final boolean isDark;

    StyleType(String name, boolean isDark) {
        this.name = BASE_PATH + name + ".xml";
        this.isDark = isDark;
    }

    @NotNull
    public static StyleType parse(String name, boolean isDark) {
        for (StyleType styleType : StyleType.values()) {
            if (styleType.name.equals(name)) {
                return styleType;
            }
        }
        return isDark ? StyleType.DARK : StyleType.DEFAULT;
    }

    /**
     * 获取所有的亮色样式
     *
     * @return Arrays
     */
    @NotNull
    public static StyleType[] getLightStyles() {
        return Arrays.stream(StyleType.values()).filter(styleType -> !styleType.isDark).toArray(StyleType[]::new);
    }

    /**
     * 获取所有的暗色样式
     *
     * @return Arrays
     */
    @NotNull
    public static StyleType[] getDarkStyles() {
        return Arrays.stream(StyleType.values()).filter(styleType -> styleType.isDark).toArray(StyleType[]::new);
    }
}
