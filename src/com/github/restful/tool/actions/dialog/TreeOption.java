package com.github.restful.tool.actions.dialog;

import com.github.restful.tool.actions.EditorOption;
import com.github.restful.tool.utils.RestUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface TreeOption extends EditorOption {

    PsiClass[] PSI_CLASSES = new PsiClass[1];

    /**
     * 获取当前PsiCLass
     *
     * @param e AnActionEvent
     * @return PsiClass
     */
    @Nullable
    static PsiClass getPsiClass(@NotNull AnActionEvent e) {
        PsiElement currentEditorElement = EditorOption.getCurrentEditorElement(e);
        if (currentEditorElement != null) {
            PsiElement context = currentEditorElement.getContext();
            if (context instanceof PsiClass) {
                return ((PsiClass) context);
            }
        }
        return null;
    }

    /**
     * 获取当前PsiCLass
     *
     * @return PsiClass
     */
    default PsiClass getPsiClass() {
        return PSI_CLASSES[0];
    }

    /**
     * 是否包含PsiClass
     *
     * @param e AnActionEvent
     * @return bool
     */
    default boolean withPsiClass(@NotNull AnActionEvent e) {
        if (e.getProject() == null) {
            return false;
        }
        PSI_CLASSES[0] = getPsiClass(e);
        return RestUtil.hasRestful(PSI_CLASSES[0]);
    }
}
