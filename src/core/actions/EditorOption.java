/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: Test
  Author:   ZhangYuanSheng
  Date:     2020/7/7 00:16
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package core.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import core.beans.Request;
import core.utils.RestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface EditorOption {

    PsiMethod[] PSI_METHODS = new PsiMethod[1];

    /**
     * 获取当前选择的PsiMethod
     *
     * @param e AnActionEvent
     * @return PsiMethod
     */
    @Nullable
    static PsiMethod getPsiMethod(@NotNull AnActionEvent e) {
        Editor editor = e.getData(LangDataKeys.EDITOR);
        if (editor == null) {
            return null;
        }
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            return null;
        }
        PsiElement currElement = psiFile.findElementAt(editor.getCaretModel().getOffset());
        if (currElement == null) {
            return null;
        }
        // 如果右键处为当前方法其中的 注解末尾 或 方法体中
        PsiElement currContext = currElement.getContext();
        if (currContext != null) {
            if (currContext instanceof PsiClass) {
                return null;
            }
            if (currContext instanceof PsiMethod) {
                return ((PsiMethod) currContext);
            }
            if (currContext instanceof PsiAnnotation) {
                PsiElement context = currContext.getContext();
                if (context != null) {
                    PsiElement contextParent = context.getParent();
                    if (contextParent instanceof PsiMethod) {
                        return ((PsiMethod) contextParent);
                    }
                }
            }
            PsiElement parentElem = currContext.getParent();
            if (parentElem instanceof PsiMethod) {
                return (PsiMethod) parentElem;
            }
            if (parentElem instanceof PsiAnnotation) {
                PsiElement context = parentElem.getContext();
                if (context != null) {
                    PsiElement contextParent = context.getParent();
                    if (contextParent instanceof PsiMethod) {
                        return ((PsiMethod) contextParent);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 检测当前 PsiClass 是否含有`RestController` | `Controller` | `Path`
     *
     * @param psiMethod psiMethod
     * @return bool
     */
    static boolean hasRestful(@Nullable PsiMethod psiMethod) {
        if (psiMethod == null) {
            return false;
        }
        return RestUtil.hasRestful(psiMethod.getContainingClass());
    }

    /**
     * 获取Request
     *
     * @param project   project
     * @param psiMethod psiMethod
     * @return Request
     */
    @Nullable
    default Request getRequest(@NotNull Project project, @NotNull PsiMethod psiMethod) {
        for (Map.Entry<String, List<Request>> entry : RestUtil.getAllRequest(project).entrySet()) {
            for (Request request : entry.getValue()) {
                if (request.getPsiMethod().equals(psiMethod)) {
                    return request;
                }
            }
        }
        return null;
    }

    /**
     * 获取PsiMethod
     *
     * @return psiMethod
     */
    default PsiMethod getPsiMethod() {
        return PSI_METHODS[0];
    }

    /**
     * 是否包含PsiMethod
     *
     * @param e AnActionEvent
     * @return bool
     */
    default boolean withPsiMethod(@NotNull AnActionEvent e) {
        if (e.getProject() == null) {
            return false;
        }
        PSI_METHODS[0] = getPsiMethod(e);
        return hasRestful(PSI_METHODS[0]);
    }
}
