package com.github.restful.tool.beans;

import com.intellij.icons.AllIcons;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ModuleTree {

    /**
     * 模块名称
     */
    private final String moduleName;

    /**
     * 图标
     */
    private final Icon icon;

    public ModuleTree(String moduleName) {
        this(moduleName, AllIcons.Modules.SourceRoot);
    }

    public ModuleTree(String moduleName, Icon icon) {
        this.moduleName = moduleName;
        this.icon = icon;
    }

    public String getModuleName() {
        return moduleName;
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return moduleName;
    }
}
