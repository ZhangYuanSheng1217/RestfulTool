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

import com.github.restful.tool.utils.Bundle;
import com.github.restful.tool.view.window.RestfulToolWindowFactory;
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

    public RefreshAction() {
        getTemplatePresentation().setText(Bundle.getString("action.Refresh.text"));
        getTemplatePresentation().setIcon(AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        RightToolWindow toolWindow = RestfulToolWindowFactory.getToolWindow(e.getProject());
        if (toolWindow == null) {
            return;
        }
        toolWindow.refreshRequestTree();
    }
}
