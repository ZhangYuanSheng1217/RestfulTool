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

import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.utils.Bundle;
import com.github.restful.tool.view.window.RestfulToolWindowFactory;
import com.github.restful.tool.view.window.frame.HttpMethodFilterPopup;
import com.github.restful.tool.view.window.frame.RightToolWindow;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ScanFilterAction extends DumbAwareAction {

    private final HttpMethodFilterPopup filterPopup;

    private RightToolWindow toolWindow;

    public ScanFilterAction() {
        filterPopup = new HttpMethodFilterPopup(HttpMethod.values());
        filterPopup.setChangeCallback((checkBox, method) -> {
            RightToolWindow.METHOD_CHOOSE_MAP.put(method, checkBox.isSelected());
            refreshTree();
        });
        filterPopup.setChangeAllCallback((ts, selected) -> {
            for (HttpMethod method : ts) {
                RightToolWindow.METHOD_CHOOSE_MAP.put(method, selected);
            }
            refreshTree();
        });

        getTemplatePresentation().setText(Bundle.getString("action.ScanFilter.text"));
        getTemplatePresentation().setIcon(AllIcons.General.Filter);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        if (getToolWindow(project) == null) {
            return;
        }
        filterPopup.show(toolWindow, 0, filterPopup.getY());
    }

    private RightToolWindow getToolWindow(@Nullable Project project) {
        if (toolWindow != null) {
            return toolWindow;
        }
        return (toolWindow = RestfulToolWindowFactory.getToolWindow(project));
    }

    private void refreshTree() {
        if (toolWindow != null) {
            toolWindow.refreshRequestTree();
        }
    }
}
