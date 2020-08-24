/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RefreshAction
  Author:   ZhangYuanSheng
  Date:     2020/8/18 15:34
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.actions;

import com.github.restful.tool.view.window.frame.RightToolWindow;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RefreshAction extends DumbAwareAction {

    private final RightToolWindow toolWindow;

    public RefreshAction(RightToolWindow toolWindow) {
        this.toolWindow = toolWindow;
        this.getTemplatePresentation().setIcon(AllIcons.Actions.Refresh);
        this.getTemplatePresentation().setText("Refresh");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        toolWindow.refreshRequestTree();
    }
}
