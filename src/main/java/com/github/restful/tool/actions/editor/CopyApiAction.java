package com.github.restful.tool.actions.editor;

import com.github.restful.tool.utils.Actions;
import com.github.restful.tool.utils.data.Bundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class CopyApiAction extends DumbAwareAction {

    public CopyApiAction() {
        getTemplatePresentation().setText(Bundle.getString("action.CopyApi.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Actions.copyPath(e, false);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        event.getPresentation().setEnabled(Actions.getPsiMethod(event) != null);
    }
}
