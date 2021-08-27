package com.github.restful.tool.actions.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public abstract class BaseIntentionAction extends PsiElementBaseIntentionAction {

    protected BaseIntentionAction(@NonNls @NotNull String text) {
        super.setText(text);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return getText();
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    protected final boolean isPsiMethod(@Nullable PsiElement element) {
        if (element == null) {
            return false;
        }
        final PsiElement parent = element.getParent();
        return parent instanceof PsiMethod;
    }
}
