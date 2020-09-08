/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: ServiceStub
  Author:   ZhangYuanSheng
  Date:     2020/9/8 05:29
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.beans;

import com.github.restful.tool.utils.SystemUtil;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ServiceStub {

    private final Set<HttpMethod> methods;
    private final Set<String> paths;

    /**
     * PsiMethod | KtNamedFunction
     *
     * @see com.intellij.psi.PsiMethod
     * @see org.jetbrains.kotlin.psi.KtNamedFunction
     */
    private NavigatablePsiElement psiElement;

    public ServiceStub() {
        this.methods = new HashSet<>();
        this.paths = new HashSet<>();
    }

    public ServiceStub(@NotNull NavigatablePsiElement psiElement) {
        this();
        this.psiElement = psiElement;
    }

    public ServiceStub method(@NotNull HttpMethod method) {
        this.methods.add(method);
        return this;
    }

    public ServiceStub path(@Nullable String path) {
        this.paths.add(SystemUtil.formatPath(path));
        return this;
    }

    @NotNull
    public List<HttpMethod> getMethods() {
        List<HttpMethod> list = new ArrayList<>(methods);
        // 当定义了具体的请求方式时，REQUEST失效
        if (list.contains(HttpMethod.REQUEST) && list.size() > 1) {
            list.remove(HttpMethod.REQUEST);
        }
        return list;
    }

    @NotNull
    public List<String> getPaths() {
        return new ArrayList<>(paths);
    }

    @Nullable
    public NavigatablePsiElement getPsiElement() {
        return psiElement;
    }

    public void setPsiElement(@NotNull NavigatablePsiElement psiElement) {
        this.psiElement = psiElement;
    }

    @Nullable
    public PsiMethod getPsiMethod() {
        if (this.psiElement instanceof PsiMethod) {
            return ((PsiMethod) this.psiElement);
        }
        return null;
    }

    @Nullable
    public KtNamedFunction getKtNamedFunction() {
        if (this.psiElement instanceof KtNamedFunction) {
            return ((KtNamedFunction) this.psiElement);
        }
        return null;
    }

    public final void navigate() {
        if (this.psiElement == null) {
            return;
        }
        if (!this.psiElement.canNavigate()) {
            return;
        }
        this.psiElement.navigate(true);
    }
}
