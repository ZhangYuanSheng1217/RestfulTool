package com.github.restful.tool.actions;

import com.github.restful.tool.view.window.WindowFactory;
import com.github.restful.tool.view.window.frame.ModuleUrlPopup;
import com.github.restful.tool.view.window.frame.Window;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public class ModuleUrlActon extends DumbAwareAction {

    private Window toolWindow;

    public ModuleUrlActon() {
        super();
        getTemplatePresentation().setText("Module Url");
        getTemplatePresentation().setIcon(AllIcons.General.Settings);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        if (getToolWindow(project) == null) {
            return;
        }
        ModuleUrlPopup popup = new ModuleUrlPopup(project);
        popup.show(toolWindow, 0, popup.getY());

    }

    private Window getToolWindow(@Nullable Project project) {
        if (toolWindow == null) {
            toolWindow = WindowFactory.getToolWindow(project);
        }
        return toolWindow;
    }
}
