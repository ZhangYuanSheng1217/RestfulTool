package com.github.restful.tool.actions.intention;

import com.github.restful.tool.utils.Actions;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class NavigationApiServiceIntentionAction extends BaseIntentionAction {

    public NavigationApiServiceIntentionAction() {
        super("Go to ApiServices view (Restful Tool)");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        element = element.getParent();
        if (!(element instanceof PsiMethod)) {
            HintManager.getInstance().showErrorHint(editor, "Can only choose method.");
            return;
        }
        Actions.gotoApiServiceTree(((PsiMethod) element));
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return isPsiMethod(element);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
