package com.github.restful.tool.view.components.tree;

import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class AbstractNode<T> extends DefaultMutableTreeNode {

    @NotNull
    private T source;

    public AbstractNode(@NotNull T source) {
        super(source);
        this.source = source;
    }

    public @NotNull T getSource() {
        return source;
    }

    public void setSource(@NotNull T source) {
        this.source = source;
    }
}
