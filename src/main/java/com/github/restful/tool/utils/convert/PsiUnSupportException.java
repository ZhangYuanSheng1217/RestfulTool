/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: PsiUnSupportException
  Author:   ZhangYuanSheng
  Date:     2020/9/10 01:08
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils.convert;

import com.intellij.psi.NavigatablePsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class PsiUnSupportException extends RuntimeException {

    public PsiUnSupportException() {
        super("Unsupported PsiElement instance resolution");
    }

    public PsiUnSupportException(@NotNull NavigatablePsiElement psiElement) {
        super("Unsupported PsiElement instance resolution: " + psiElement.getName());
    }
}
