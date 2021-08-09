package com.github.restful.tool.actions;

import com.github.restful.tool.utils.Actions;
import com.github.restful.tool.view.window.WindowFactory;
import com.github.restful.tool.view.window.frame.Window;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class TreeExpandAction extends DumbAwareAction {

    public TreeExpandAction() {
        Actions.applyActionInfo(this, "action.TreeExpandAction.text", AllIcons.Toolbar.Expand);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        Window window = WindowFactory.getToolWindow(project);
        if (window == null) {
            return;
        }
        window.expandAll(true);
    }
}
