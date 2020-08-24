package com.github.restful.tool.service;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface ToolWindowService {

    /**
     * getInstance
     *
     * @param project project
     * @return obj
     */
    static ToolWindowService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ToolWindowService.class);
    }

    /**
     * get view content
     *
     * @return ContentView
     */
    JComponent getContent();

    /**
     * init window
     *
     * @param toolWindow toolWindow
     */
    default void init(@NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(getContent(), "", false);

        toolWindow.getContentManager().addContent(content);
    }
}
