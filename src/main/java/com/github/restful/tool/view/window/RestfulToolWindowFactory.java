/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RestfulToolWindow
  Author:   ZhangYuanSheng
  Date:     2020/4/29 15:06
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window;

import com.github.restful.tool.service.ToolWindowService;
import com.github.restful.tool.utils.Constants;
import com.github.restful.tool.view.window.frame.RightToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestfulToolWindowFactory implements ToolWindowFactory {

    /**
     * ToolWindowId
     */
    public static final String TOOL_WINDOW_ID = Constants.Application.NAME;

    /**
     * 获取RestfulTool的toolWindow窗口内容
     *
     * @param project auto
     * @return RightToolWindow
     */
    @Nullable
    public static RightToolWindow getToolWindow(@Nullable Project project) {
        return getToolWindow(project, null);
    }

    /**
     * 获取RestfulTool的toolWindow窗口内容
     *
     * @param project auto
     * @return RightToolWindow
     */
    @Nullable
    public static RightToolWindow getToolWindow(@Nullable Project project, Boolean show) {
        if (project == null) {
            return null;
        }
        ToolWindow toolWindow = getWindow(project);
        if (show != null && show) {
            showWindow(project, null);
        }
        if (toolWindow != null) {
            for (Component component : toolWindow.getComponent().getComponents()) {
                if (component instanceof RightToolWindow) {
                    return ((RightToolWindow) component);
                }
            }
        }
        return null;
    }

    @Nullable
    public static ToolWindow getWindow(@NotNull Project project) {
        return ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
    }

    public static void showWindow(@NotNull Project project, @Nullable Runnable onShow) {
        ToolWindow window = getWindow(project);
        if (window == null) {
            return;
        }
        window.show(onShow);
    }

    public static void hideWindow(@NotNull Project project, @Nullable Runnable onShow) {
        ToolWindow window = getWindow(project);
        if (window == null) {
            return;
        }
        window.hide(onShow);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ToolWindowService.getInstance(project).init(toolWindow);
    }
}
