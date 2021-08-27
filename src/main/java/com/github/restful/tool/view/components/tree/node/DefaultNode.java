package com.github.restful.tool.view.components.tree.node;

import com.github.restful.tool.view.components.tree.BaseNode;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class DefaultNode<T> extends BaseNode<T> {

    public DefaultNode(@NotNull T source) {
        super(source);
    }

    @Override
    public @NotNull String getFragment() {
        return String.valueOf(getSource());
    }

    @Override
    public @NotNull SimpleTextAttributes getTextAttributes() {
        return super.getTextAttributes();
    }
}
