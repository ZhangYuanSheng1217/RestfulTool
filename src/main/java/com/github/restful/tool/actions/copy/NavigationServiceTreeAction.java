package com.github.restful.tool.actions.copy;

import com.github.restful.tool.utils.Bundle;
import com.github.restful.tool.view.icon.Icons;
import com.github.restful.tool.view.window.RestfulToolWindowFactory;
import com.github.restful.tool.view.window.frame.RightToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class NavigationServiceTreeAction extends AnAction implements CopyOption {

    private RightToolWindow toolWindow;

    public NavigationServiceTreeAction() {
        getTemplatePresentation().setText(Bundle.getString("action.NavigateToView.text"));
        getTemplatePresentation().setIcon(Icons.Plugin);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiMethod psiMethod = getPsiMethod(e);
        if (psiMethod == null) {
            return;
        }
        if (getToolWindow(e.getProject()) == null) {
            return;
        }
        toolWindow.navigationToView(psiMethod);
    }

    private RightToolWindow getToolWindow(@Nullable Project project) {
        if (toolWindow != null) {
            return toolWindow;
        }
        return (toolWindow = RestfulToolWindowFactory.getToolWindow(project, true));
    }
}
