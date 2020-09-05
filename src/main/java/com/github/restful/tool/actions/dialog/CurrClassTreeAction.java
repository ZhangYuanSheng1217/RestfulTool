/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: CurrClassTreeAction
  Author:   ZhangYuanSheng
  Date:     2020/7/7 16:41
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.actions.dialog;

import com.github.restful.tool.beans.Request;
import com.github.restful.tool.utils.Bundle;
import com.github.restful.tool.utils.RestUtil;
import com.github.restful.tool.view.window.frame.ServiceTree;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class CurrClassTreeAction extends AnAction implements TreeOption {

    private final Map<String, List<Request>> requests;
    private ServiceTree serviceTree;

    public CurrClassTreeAction() {
        this.requests = new HashMap<>(1);
        getTemplatePresentation().setText(Bundle.getString("action.ShowCurrClassServiceTree.text"));
        getTemplatePresentation().setDescription(Bundle.getString("action.ShowCurrClassServiceTree.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiClass psiClass = getPsiClass();
        if (project == null || psiClass == null) {
            return;
        }
        if (serviceTree == null) {
            this.serviceTree = new ServiceTree(project);
        }
        List<Request> requests = RestUtil.getCurrClassRequests(psiClass);
        serviceTree.renderRequestTree(format(psiClass, requests));
        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(serviceTree, null);
        JBPopup popup = popupBuilder.createPopup();
        popup.setMinimumSize(new Dimension(300, serviceTree.getSize().height));
        popup.showInFocusCenter();
    }

    private Map<String, List<Request>> format(@NotNull PsiClass psiClass, @NotNull List<Request> requests) {
        if (!this.requests.isEmpty()) {
            this.requests.clear();
        }
        this.requests.put(psiClass.getName(), requests);
        return this.requests;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(withPsiClass(e));
    }
}
