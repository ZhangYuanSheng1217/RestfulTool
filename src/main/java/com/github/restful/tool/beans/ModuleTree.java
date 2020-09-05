/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: ModuleTree
  Author:   ZhangYuanSheng
  Date:     2020/8/25 16:48
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
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
     * 接口数量
     */
    private final Integer apiCounts;

    /**
     * 图标
     */
    private final Icon icon;

    public ModuleTree(String moduleName, Integer apiCounts) {
        this.moduleName = moduleName;
        this.apiCounts = apiCounts;
        this.icon = AllIcons.Modules.SourceRoot;
    }

    public ModuleTree(String moduleName, Integer apiCounts, Icon icon) {
        this.moduleName = moduleName;
        this.apiCounts = apiCounts;
        this.icon = icon;
    }

    public String getModuleName() {
        return moduleName;
    }

    public Integer getApiCounts() {
        return apiCounts;
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return String.format(
                "[%d]%s",
                apiCounts,
                moduleName
        );
    }
}
