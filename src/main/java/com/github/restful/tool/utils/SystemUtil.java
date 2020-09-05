/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: SystemUtil
  Author:   ZhangYuanSheng
  Date:     2020/5/26 01:03
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class SystemUtil {

    private static final String SLASH = "/";

    /**
     * 生成url
     *
     * @param protocol    协议
     * @param port        端口
     * @param contextPath 访问根目录名
     * @param path        路径
     * @return url
     */
    @NotNull
    public static String buildUrl(@NotNull String protocol, @Nullable Integer port, @Nullable String contextPath, String path) {
        StringBuilder url = new StringBuilder(protocol + "://");
        url.append("localhost");
        if (port != null) {
            url.append(":").append(port);
        }
        if (contextPath != null && !"null".equals(contextPath) && contextPath.startsWith(SLASH)) {
            url.append(contextPath);
        }
        if (!path.startsWith(SLASH)) {
            url.append(SLASH);
        }
        url.append(path);
        return url.toString();
    }

    /**
     * 格式化request path
     *
     * @param path path
     * @return format path
     */
    @NotNull
    @Contract(pure = true)
    public static String formatPath(@Nullable Object path) {
        if (path == null) {
            return SLASH;
        }
        String currPath;
        if (path instanceof String) {
            currPath = (String) path;
        } else {
            currPath = path.toString();
        }
        if (currPath.startsWith(SLASH)) {
            return currPath;
        }
        return SLASH + currPath;
    }

    /**
     * 剪贴板工具
     */
    public static class Clipboard {

        /**
         * 把文本设置到剪贴板（复制）
         */
        public static void copy(String text) {
            // 获取系统剪贴板
            java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            // 封装文本内容
            Transferable trans = new StringSelection(text);
            // 把文本内容设置到系统剪贴板
            clipboard.setContents(trans, null);
        }

        /**
         * 从剪贴板中获取文本（粘贴）
         */
        @Nullable
        public static String paste() {
            // 获取系统剪贴板
            java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            // 获取剪贴板中的内容
            Transferable trans = clipboard.getContents(null);
            if (trans != null) {
                // 判断剪贴板中的内容是否支持文本
                if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    try {
                        // 获取剪贴板中的文本内容
                        return (String) trans.getTransferData(DataFlavor.stringFlavor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }

    /**
     * 数组工具
     */
    public static class Array {

        /**
         * 获取合法的数组下标
         *
         * @param array 数组
         * @param index 给定的下标（可能越界）
         * @param <T>   泛型
         * @return 下标
         */
        public static <T> int getLegalSubscript(@NotNull T[] array, Integer index) {
            if (index == null) {
                return 0;
            }
            if (index < 0) {
                return 0;
            }
            if (index >= array.length) {
                return array.length - 1;
            }
            return index;
        }
    }
}
