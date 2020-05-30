/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: IconType
  Author:   ZhangYuanSheng
  Date:     2020/5/31 01:22
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package core.view.icon;

import core.beans.HttpMethod;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface IconType {

    /**
     * 默认显示
     *
     * @param method method
     * @return default
     */
    @NotNull
    Icon getDefaultIcon(HttpMethod method);

    /**
     * 选中图标
     *
     * @param method method
     * @return select
     */
    @NotNull
    Icon getSelectIcon(HttpMethod method);

    /**
     * 获取默认图标列表
     *
     * @return list
     */
    @NotNull
    List<PreviewIcon> getDefaultIcons();

    /**
     * 获取选中图标列表
     *
     * @return list
     */
    @NotNull
    List<PreviewIcon> getSelectIcons();

    /**
     * 图标名
     *
     * @return name
     */
    @Override
    @NotNull
    String toString();

    /**
     * 获取排序后的图标列表
     *
     * @return list
     */
    default List<PreviewIcon> getSortDefaultIcons() {
        return this.getDefaultIcons().stream().sorted(new IconComparator()).collect(Collectors.toList());
    }

    /**
     * 获取排序后的图标列表
     *
     * @return list
     */
    default List<PreviewIcon> getSortSelectIcons() {
        return this.getSelectIcons().stream().sorted(new IconComparator()).collect(Collectors.toList());
    }
}

class IconComparator implements Comparator<PreviewIcon> {

    @Override
    public int compare(@NotNull PreviewIcon o1, @NotNull PreviewIcon o2) {
        char[] chars1 = o1.getText().toCharArray();
        char[] chars2 = o2.getText().toCharArray();

        int maxLen = Math.min(chars1.length, chars2.length);
        for (int i = 0; i < maxLen; i++) {
            if (chars1[i] != chars2[i]) {
                return chars1[i] - chars2[i];
            }
        }

        return chars1.length - chars2.length;
    }
}
