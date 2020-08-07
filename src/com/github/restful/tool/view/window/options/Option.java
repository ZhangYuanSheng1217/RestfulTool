/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: Option
  Author:   ZhangYuanSheng
  Date:     2020/8/6 17:30
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window.options;

import com.github.restful.tool.beans.settings.AppSetting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface Option {

    /**
     * 显示设置
     *
     * @param setting 设置信息
     */
    void showSetting(@NotNull AppSetting setting);

    /**
     * 应用设置
     *
     * @param setting 设置信息
     */
    void applySetting(@NotNull AppSetting setting);

    /**
     * 获取顶部间距
     *
     * @return int
     */
    @Nullable
    default Integer getTopInset() {
        return null;
    }

    interface Custom<T extends JComponent> {

        /**
         * 显示设置
         *
         * @param setting   设置信息
         * @param component 组件
         * @return true: 启用 | false: 执行原本逻辑
         */
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean showSetting(@NotNull AppSetting setting, @NotNull T component);

        /**
         * 应用设置
         *
         * @param setting   设置信息
         * @param component 组件
         * @return true: 启用 | false: 执行原本逻辑
         */
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean applySetting(@NotNull AppSetting setting, @NotNull T component);

        /**
         * 获取顶部间距
         *
         * @return int
         */
        @Nullable
        default Integer getTopInset() {
            return null;
        }
    }
}
