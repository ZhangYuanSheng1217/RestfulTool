package com.github.restful.tool.view.components.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public abstract class BaseNode<T> extends AbstractNode<T> implements ISource {

    public BaseNode(@NotNull T source) {
        super(source);
    }

    @Nullable
    public Icon getIcon(boolean selected) {
        return null;
    }

    @Override
    public void add(@NotNull MutableTreeNode newChild) {
        if (!(newChild instanceof BaseNode<?>)) {
            return;
        }
        addNode(((BaseNode<?>) newChild));
    }

    private void addNode(@NotNull BaseNode<?> newChild) {
        super.add(newChild);
    }
}
