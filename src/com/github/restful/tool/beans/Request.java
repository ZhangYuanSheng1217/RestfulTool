/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: Request
  Author:   ZhangYuanSheng
  Date:     2020/5/2 00:43
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.beans;

import com.github.restful.tool.view.icon.Icons;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class Request {

    private final PsiMethod psiMethod;
    @Nullable
    private HttpMethod method;
    @Nullable
    private String path;
    @NotNull
    private Icon icon = Icons.getMethodIcon(null);

    public Request(HttpMethod method, @Nullable String path, @Nullable PsiMethod psiMethod) {
        this.setMethod(method);
        if (path != null) {
            this.setPath(path);
        }
        this.psiMethod = psiMethod;
    }

    public PsiMethod getPsiMethod() {
        return psiMethod;
    }

    public void navigate(boolean requestFocus) {
        if (psiMethod != null) {
            psiMethod.navigate(requestFocus);
        }
    }

    @Nullable
    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(@Nullable HttpMethod method) {
        this.method = method;
        this.icon = Icons.getMethodIcon(method);
    }

    @NotNull
    public Icon getIcon() {
        return icon;
    }

    public Icon getSelectIcon() {
        return Icons.getMethodIcon(this.method, true);
    }

    @Nullable
    public String getPath() {
        return path;
    }

    public void setPath(@NotNull String path) {
        path = path.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        path = path.replaceAll("//", "/");
        this.path = path;
    }

    public void setParent(@NotNull Request parent) {
        if (this.method == null && parent.getMethod() != null) {
            this.setMethod(parent.getMethod());
        }
        String parentPath = parent.getPath();
        if (parentPath != null && parentPath.endsWith("/")) {
            // 去掉末尾的斜杠
            parentPath = parentPath.substring(0, parentPath.length() - 1);
        }
        this.setPath(parentPath + this.path);
    }

    @NotNull
    public Request copyWithParent(@Nullable Request parent) {
        Request request = new Request(this.method, this.path, this.psiMethod);
        if (parent != null) {
            request.setParent(parent);
        }
        return request;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Request request = (Request) o;

        if (!psiMethod.equals(request.psiMethod)) {
            return false;
        }
        if (method != request.method) {
            return false;
        }
        if (!Objects.equals(path, request.path)) {
            return false;
        }
        return icon.equals(request.icon);
    }

    @Override
    public int hashCode() {
        int result = psiMethod.hashCode();
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + icon.hashCode();
        return result;
    }

    @NotNull
    public String getIdentity(String... itemIds) {
        HttpMethod method = this.method == null ? HttpMethod.REQUEST : this.method;
        String path = this.path == null ? "" : this.path;

        StringBuilder items = new StringBuilder();
        if (itemIds != null) {
            items.append("-[");
            for (int i = 0; i < itemIds.length; i++) {
                if (i > 0) {
                    items.append(", ");
                }
                items.append(itemIds[i]);
            }
            items.append("]");
        }

        return String.format("{}[%s]%s(%s)%s", method, path, icon.getClass(), items.toString());
    }

    @Override
    public String toString() {
        return this.getIdentity();
    }
}
