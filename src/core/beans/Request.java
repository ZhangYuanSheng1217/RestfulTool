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
package core.beans;

import com.intellij.psi.PsiMethod;
import core.view.icon.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class Request {

    private HttpMethod method;
    private String path;
    private Icon icon;

    private final PsiMethod psiMethod;

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

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
        this.icon = Icons.getMethodIcon(method);
    }

    public Icon getIcon() {
        return icon;
    }

    public Icon getSelectIcon() {
        return Icons.getMethodIcon(this.method, true);
    }

    public String getPath() {
        return path;
    }

    public void setPath(@NotNull String path) {
        path = path.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        this.path = path;
    }

    public void setParent(@NotNull Request parent) {
        if (this.method == null && parent.getMethod() != null) {
            this.setMethod(parent.getMethod());
        }
        String parentPath = parent.getPath();
        if (parentPath != null && parentPath.endsWith("/")) {
            parentPath = "";
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
    public String toString() {
        return "[" + method + "]" + path + "(" + icon + ")";
    }
}
