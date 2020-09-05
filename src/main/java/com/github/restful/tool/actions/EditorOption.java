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
package com.github.restful.tool.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface EditorOption {

    /**
     * 获取当前正在编辑行的elem
     *
     * @param e AnActionEvent
     * @return PsiElement
     */
    @Nullable
    static PsiElement getCurrentEditorElement(@NotNull AnActionEvent e) {
        Editor editor = e.getData(LangDataKeys.EDITOR);
        if (editor == null) {
            return null;
        }
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            return null;
        }
        return psiFile.findElementAt(editor.getCaretModel().getOffset());
    }
}
