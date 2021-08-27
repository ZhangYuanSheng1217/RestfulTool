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

import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.beans.ClassTree;
import com.github.restful.tool.service.Notify;
import com.github.restful.tool.utils.Actions;
import com.github.restful.tool.utils.ApiServices;
import com.github.restful.tool.utils.data.Bundle;
import com.github.restful.tool.view.components.tree.BaseNode;
import com.github.restful.tool.view.window.frame.ApiServiceListPanel;
import com.github.restful.tool.view.components.tree.node.ClassNode;
import com.github.restful.tool.view.components.tree.node.ModuleNode;
import com.github.restful.tool.view.components.tree.node.RootNode;
import com.github.restful.tool.view.components.tree.node.ServiceNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class CurrClassTreeAction extends AnAction {

    private final Map<PsiClass, List<ApiService>> requests;
    private ApiServiceListPanel serviceListPanel;

    public CurrClassTreeAction() {
        this.requests = new HashMap<>(1);
        getTemplatePresentation().setText(Bundle.getString("action.ShowCurrClassServiceTree.text"));
        getTemplatePresentation().setDescription(Bundle.getString("action.ShowCurrClassServiceTree.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        List<PsiClass> psiClasses = Actions.getPsiClass(e);
        if (project == null || psiClasses.isEmpty()) {
            return;
        }
        if (serviceListPanel == null) {
            this.serviceListPanel = new ApiServiceListPanel(project);
        }

        if (!renderData(psiClasses)) {
            Notify.getInstance(project).warning("Cannot find any Apis");
            return;
        }

        RootNode root = new RootNode("Find empty");
        Map<PsiMethod, ServiceNode> serviceNodes = new HashMap<>();
        this.requests.forEach((psiClass, apiServices) -> {
            ClassNode classNode = new ClassNode(new ClassTree(psiClass));
            List<BaseNode<?>> nodes = ModuleNode.Util.getChildren(serviceNodes, apiServices);
            nodes.forEach(classNode::add);
            root.add(classNode);
        });
        if (!serviceNodes.isEmpty()) {
            root.setSource("Find " + serviceNodes.size() + " apis");
        }
        serviceListPanel.renderAll(root, serviceNodes, true);

        ComponentPopupBuilder popupBuilder = JBPopupFactory
                .getInstance()
                .createComponentPopupBuilder(serviceListPanel, null);
        JBPopup popup = popupBuilder.createPopup();
        popup.setMinimumSize(new Dimension(300, serviceListPanel.getSize().height));
        popup.showInFocusCenter();
    }

    private boolean renderData(@NotNull List<PsiClass> psiClasses) {
        if (!this.requests.isEmpty()) {
            this.requests.clear();
        }

        for (PsiClass psiClass : psiClasses) {
            List<ApiService> apiServices = ApiServices.getCurrClassRequests(psiClass);
            if (apiServices.isEmpty()) {
                continue;
            }
            this.requests.put(psiClass, apiServices);
        }

        return !this.requests.isEmpty();
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        event.getPresentation().setEnabled(!Actions.getPsiClass(event).isEmpty());
    }
}
