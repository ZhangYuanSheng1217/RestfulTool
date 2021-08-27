package com.github.restful.tool.view.components.tree.node;

import cn.hutool.core.util.StrUtil;
import com.github.restful.tool.beans.ApiService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ServiceNode extends DefaultNode<ApiService> {

    public ServiceNode(@NotNull ApiService source) {
        super(source);
    }

    @Override
    public @Nullable Icon getIcon(boolean selected) {
        if (selected) {
            return getSource().getSelectIcon();
        } else {
            return getSource().getIcon();
        }
    }

    @Override
    public @NotNull String getFragment() {
        return StrUtil.trimToEmpty(getSource().getPath());
    }
}
