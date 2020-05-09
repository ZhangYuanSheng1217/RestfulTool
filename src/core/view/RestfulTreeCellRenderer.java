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
package core.view;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import core.beans.Request;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

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
        Object obj = ((DefaultMutableTreeNode) value).getUserObject();
        if (obj instanceof Request) {
            Request node = (Request) obj;

            setMethodTypeAndPath(node);
        } else if (obj instanceof String) {
            append((String) obj, SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES);
        } else if (obj instanceof Integer) {
            setIcon(Icons.SERVICE);
            append(String.format("共找到%s个服务接口", obj));
        }
    }

    private void setMethodTypeAndPath(@Nullable Request node) {
        if (node == null) {
            return;
        }
        setIcon(Icons.getMethodIcon(node.getMethod()));
        append(node.getPath());
    }
}
