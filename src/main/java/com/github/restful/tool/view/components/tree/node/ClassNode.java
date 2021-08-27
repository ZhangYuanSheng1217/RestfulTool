package com.github.restful.tool.view.components.tree.node;

import com.github.restful.tool.beans.ClassTree;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ClassNode extends DefaultNode<ClassTree> {

    public ClassNode(@NotNull ClassTree source) {
        super(source);
    }

    @Override
    public @Nullable Icon getIcon(boolean selected) {
        return AllIcons.Nodes.Class;
    }

    @Override
    public @NotNull String getFragment() {
        return getSource().getName();
    }
}
