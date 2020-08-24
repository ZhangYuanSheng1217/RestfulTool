/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: WithLibraryAction
  Author:   ZhangYuanSheng
  Date:     2020/8/21 17:20
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.actions;

import com.github.restful.tool.beans.PropertiesKey;
import com.github.restful.tool.view.window.frame.RightToolWindow;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class WithLibraryAction extends ToggleAction implements DumbAware {

    private final RightToolWindow toolWindow;

    public WithLibraryAction(@NotNull RightToolWindow toolWindow) {
        this.toolWindow = toolWindow;

        this.getTemplatePresentation().setIcon(AllIcons.ObjectBrowser.ShowLibraryContents);
        this.getTemplatePresentation().setText("Enable Library Scanning");
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return PropertiesKey.scanServiceWithLibrary(toolWindow.getProject());
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        PropertiesKey.scanServiceWithLibrary(toolWindow.getProject(), state);
        toolWindow.refreshRequestTree();
    }
}
