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
import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.view.window.frame.ApiServiceListPanel;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ApiTreeCellRenderer extends ColoredTreeCellRenderer {

    @Override
    public void customizeCellRenderer(
            @NotNull JTree tree, Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row, boolean hasFocus) {
        if (value instanceof ApiServiceListPanel.ModuleNode) {
            ApiServiceListPanel.ModuleNode node = (ApiServiceListPanel.ModuleNode) value;
            ModuleTree data = node.getData();
            setIcon(data.getIcon());
            append(data.toString());
        } else if (value instanceof ApiServiceListPanel.ServiceNode) {
            ApiServiceListPanel.ServiceNode node = (ApiServiceListPanel.ServiceNode) value;
            ApiService data = node.getData();
            setMethodTypeAndPath(data, selected);
        } else if (value instanceof ApiServiceListPanel.ClassNode) {
            ApiServiceListPanel.ClassNode node = (ApiServiceListPanel.ClassNode) value;
            ClassTree data = node.getData();
            setIcon(data.getIcon());
            append(data.getName());
            append(" - " + data.getQualifiedName(), SimpleTextAttributes.GRAYED_ATTRIBUTES);
        } else if (value instanceof ApiServiceListPanel.TreeNode<?>) {
            ApiServiceListPanel.TreeNode<?> node = (ApiServiceListPanel.TreeNode<?>) value;
            append(node.toString());
        }
    }

    private void setMethodTypeAndPath(@Nullable ApiService node, boolean selected) {
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
