package com.github.restful.tool.view.components.tree;

import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface ISource {

    /**
     * 获取显示的字符串
     *
     * @return str
     */
    @NotNull
    String getFragment();

    @NotNull
    default SimpleTextAttributes getTextAttributes() {
        return SimpleTextAttributes.REGULAR_ATTRIBUTES;
    }
}
