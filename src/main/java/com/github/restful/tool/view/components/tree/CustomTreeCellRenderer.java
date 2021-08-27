/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RestfulTreeCellRenderer
  Author:   ZhangYuanSheng
  Date:     2020/5/6 15:41
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.components.tree;

import com.intellij.ui.ColoredTreeCellRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class CustomTreeCellRenderer extends ColoredTreeCellRenderer {

    @Override
    public void customizeCellRenderer(
            @NotNull JTree tree,
            Object target,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row, boolean hasFocus) {
        BaseNode<?> node = null;
        if (target instanceof BaseNode) {
            node = (BaseNode<?>) target;
        }
        if (node == null) {
            return;
        }
        setIcon(node.getIcon(selected));
        append(node.getFragment(), node.getTextAttributes());
    }
}
