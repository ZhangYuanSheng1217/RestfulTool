package com.github.restful.tool.view.components.tree;

import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class CustomTreeModel extends DefaultTreeModel {

    public CustomTreeModel() {
        this(null);
    }

    public CustomTreeModel(@Nullable TreeNode root) {
        super(root);
    }
}
