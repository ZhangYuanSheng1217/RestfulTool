package com.github.restful.tool.view.components.tree.node;

import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class PackageNode extends DefaultNode<String> {

    public PackageNode(@NotNull String source) {
        super(source);
    }

    @Override
    public @Nullable Icon getIcon(boolean selected) {
        return AllIcons.Nodes.Package;
    }
}
