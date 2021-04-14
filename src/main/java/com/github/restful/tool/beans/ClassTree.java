/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: ModuleTree
  Author:   ZhangYuanSheng
  Date:     2020/8/25 16:48
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.beans;

import com.intellij.icons.AllIcons;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ClassTree {

    private final PsiClass psiClass;

    /**
     * 图标
     */
    private final Icon icon;

    public ClassTree(@NotNull PsiClass psiClass) {
        this.psiClass = psiClass;
        this.icon = AllIcons.FileTypes.Java;
    }

    public ClassTree(@NotNull PsiClass psiClass, Icon icon) {
        this.psiClass = psiClass;
        this.icon = icon;
    }

    public NavigatablePsiElement getPsiClass() {
        return psiClass;
    }

    public Icon getIcon() {
        return icon;
    }

    public String getQualifiedName() {
        return psiClass.getQualifiedName();
    }

    public String getName() {
        return psiClass.getName();
    }

    public String getSimpleName() {
        String[] split = getQualifiedName().split("\\.");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            sb.append(split[i].charAt(0)).append(".");
        }
        return sb.append(split[split.length - 1]).toString();
    }

    @Override
    public String toString() {
        return getSimpleName();
    }
}
