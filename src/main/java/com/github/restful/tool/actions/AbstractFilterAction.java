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

import com.github.restful.tool.service.topic.RefreshServiceTreeTopic;
import com.github.restful.tool.view.window.WindowFactory;
import com.github.restful.tool.view.window.frame.AbstractFilterPopup;
import com.github.restful.tool.view.window.frame.Window;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public abstract class AbstractFilterAction<T> extends DumbAwareAction {

    private final AbstractFilterPopup<T> filterPopup;

    private Window toolWindow;

    private boolean hasSubstring = false;

    protected AbstractFilterAction(@NotNull String title, @Nullable Icon icon, @NotNull AbstractFilterPopup<T> filterPopup) {
        this.filterPopup = filterPopup;
        filterPopup.setChangeCallback((checkBox, item) -> {
            callback(item, checkBox.isSelected());
            refreshTree();
        });
        filterPopup.setChangeAllCallback((items, selected) -> {
            for (T item : items) {
                callback(item, selected);
            }
            refreshTree();
        });

        getTemplatePresentation().setText(title);
        getTemplatePresentation().setIcon(icon);
    }

    protected abstract void callback(T item, boolean selected);

    protected final void render(T[] values, T[] defaultValues) {
        filterPopup.render(values, defaultValues);
    }

    protected final void reset() {
        filterPopup.reset();
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
        if (!hasSubstring) {
            hasSubstring = true;
            project.getMessageBus().connect().subscribe(RefreshServiceTreeTopic.TOPIC, this::reset);
        }
        filterPopup.show(toolWindow, 0, filterPopup.getY());
    }

    private Window getToolWindow(@Nullable Project project) {
        if (toolWindow == null) {
            toolWindow = WindowFactory.getToolWindow(project);
        }
        return toolWindow;
    }

    private void refreshTree() {
        if (toolWindow != null) {
            toolWindow.refreshRequestTreeOnFilter();
        }
    }
}
