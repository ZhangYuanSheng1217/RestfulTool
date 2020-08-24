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
import com.github.restful.tool.view.window.frame.HttpMethodFilterPopup;
import com.github.restful.tool.view.window.frame.RightToolWindow;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ScanFilterAction extends DumbAwareAction {

    private final RightToolWindow toolWindow;
    private final HttpMethodFilterPopup filterPopup;

    public ScanFilterAction(RightToolWindow toolWindow) {
        this.toolWindow = toolWindow;
        this.getTemplatePresentation().setIcon(AllIcons.General.Filter);
        this.getTemplatePresentation().setText("Method Filter");

        filterPopup = new HttpMethodFilterPopup(HttpMethod.values());
        filterPopup.setChangeCallback((checkBox, method) -> {
            RightToolWindow.METHOD_CHOOSE_MAP.put(method, checkBox.isSelected());
            toolWindow.refreshRequestTree();
        });
        filterPopup.setChangeAllCallback((ts, selected) -> {
            for (HttpMethod method : ts) {
                RightToolWindow.METHOD_CHOOSE_MAP.put(method, selected);
            }
            toolWindow.refreshRequestTree();
        });
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // BrowserUtil.browse("https://www.baidu.com/")
        filterPopup.show(toolWindow, 0, filterPopup.getY());
    }
}
