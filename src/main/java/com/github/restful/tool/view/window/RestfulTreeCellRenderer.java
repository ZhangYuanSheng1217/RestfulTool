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
package com.github.restful.tool.view.window;

import com.github.restful.tool.beans.ClassTree;
import com.github.restful.tool.beans.ModuleTree;
import com.github.restful.tool.beans.Request;
import com.github.restful.tool.view.window.frame.ServiceTree;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestfulTreeCellRenderer extends ColoredTreeCellRenderer {

    @Override
    public void customizeCellRenderer(
            @NotNull JTree tree, Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row, boolean hasFocus) {
        if (value instanceof ServiceTree.ModuleNode) {
            ServiceTree.ModuleNode node = (ServiceTree.ModuleNode) value;
            ModuleTree data = node.getData();
            setIcon(data.getIcon());
            append(data.toString());
        } else if (value instanceof ServiceTree.RequestNode) {
            ServiceTree.RequestNode node = (ServiceTree.RequestNode) value;
            Request data = node.getData();
            setMethodTypeAndPath(data, selected);
        } else if (value instanceof ServiceTree.ControllerNode) {
            ServiceTree.ControllerNode node = (ServiceTree.ControllerNode) value;
            ClassTree data = node.getData();
            setIcon(data.getIcon());
            append(data.getName());
            append(" - " + data.getQualifiedName(), SimpleTextAttributes.GRAYED_ATTRIBUTES);
        } else if (value instanceof ServiceTree.TreeNode<?>) {
            ServiceTree.TreeNode<?> node = (ServiceTree.TreeNode<?>) value;
            append(node.toString());
        }
    }

    private void setMethodTypeAndPath(@Nullable Request node, boolean selected) {
        if (node == null) {
            return;
        }
        if (selected) {
            setIcon(node.getSelectIcon());
        } else {
            setIcon(node.getIcon());
        }
        String path = node.getPath();
        if (path != null) {
            append(path);
        }
    }
}
