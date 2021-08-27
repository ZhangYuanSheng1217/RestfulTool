/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: ServiceTree
  Author:   ZhangYuanSheng
  Date:     2020/7/7 23:56
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window.frame;

import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.beans.ClassTree;
import com.github.restful.tool.beans.ModuleTree;
import com.github.restful.tool.service.Notify;
import com.github.restful.tool.service.topic.RefreshServiceTreeTopic;
import com.github.restful.tool.utils.SystemUtil;
import com.github.restful.tool.utils.data.Bundle;
import com.github.restful.tool.view.components.popups.ModuleConfigPopup;
import com.github.restful.tool.view.components.popups.ModuleHeadersPopup;
import com.github.restful.tool.view.components.tree.AbstractListTreePanel;
import com.github.restful.tool.view.components.tree.BaseNode;
import com.github.restful.tool.view.components.tree.node.ClassNode;
import com.github.restful.tool.view.components.tree.node.ModuleNode;
import com.github.restful.tool.view.components.tree.node.ServiceNode;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.treeStructure.SimpleTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ApiServiceListPanel extends AbstractListTreePanel {

    private final transient Project project;

    private final transient Map<PsiMethod, ServiceNode> apiServiceNodes;

    private Consumer<ApiService> chooseCallback;

    public ApiServiceListPanel(@NotNull Project project) {
        super(new SimpleTree());
        this.project = project;
        this.apiServiceNodes = new HashMap<>();

        // 按回车键跳转到对应方法
        getTree().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    BaseNode<?> node = getChooseNode(null);
                    if (node instanceof ServiceNode) {
                        ((ServiceNode) node).getSource().navigate(true);
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    protected JPopupMenu getPopupMenu(@NotNull MouseEvent event, @NotNull BaseNode<?> node) {
        List<JMenuItem> items = new ArrayList<>();
        if (node instanceof ClassNode) {
            // navigation
            JMenuItem navigation = new JBMenuItem(Bundle.getString("action.NavigateToClass.text"), AllIcons.Nodes.Class);
            navigation.addActionListener(actionEvent -> {
                ClassTree classTree = ((ClassNode) node).getSource();
                classTree.getPsiClass().navigate(true);
            });
            items.add(navigation);
        } else if (node instanceof ServiceNode) {
            // navigation
            JMenuItem navigation = new JBMenuItem(Bundle.getString("action.NavigateToMethod.text"), AllIcons.Nodes.Method);
            navigation.addActionListener(actionEvent -> {
                ApiService apiService = ((ServiceNode) node).getSource();
                apiService.navigate(true);
            });
            items.add(navigation);

            // Copy full url
            JMenuItem copyFullUrl = new JBMenuItem(Bundle.getString("action.CopyFullPath.text"), AllIcons.Actions.Copy);
            copyFullUrl.addActionListener(actionEvent -> {
                ApiService apiService = ((ServiceNode) node).getSource();
                SystemUtil.Clipboard.copy(apiService.getRequestUrl());
                Notify.getInstance(project).info(Bundle.getString("action.CopyFullPath.success"));
            });
            items.add(copyFullUrl);

            // Copy api path
            JMenuItem copyApiPath = new JBMenuItem(Bundle.getString("action.CopyApi.text"), AllIcons.Actions.Copy);
            copyApiPath.addActionListener(actionEvent -> {
                ApiService apiService = ((ServiceNode) node).getSource();
                SystemUtil.Clipboard.copy(apiService.getPath());
                Notify.getInstance(project).info(Bundle.getString("action.CopyApi.success"));
            });
            items.add(copyApiPath);
        } else if (node instanceof ModuleNode) {
            ModuleTree moduleTree = ((ModuleNode) node).getSource();
            String moduleName = moduleTree.getModuleName();

            JBMenuItem moduleSetting = new JBMenuItem(Bundle.getString("action.OpenModuleSetting.text"), AllIcons.General.Settings);
            moduleSetting.addActionListener(action -> {
                Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
                if (module == null) {
                    return;
                }
                // 打开当前项目模块设置
                ProjectSettingsService.getInstance(project).openModuleSettings(module);
            });
            items.add(moduleSetting);

            JBMenuItem moduleConfig = new JBMenuItem(Bundle.getString("action.OpenModuleProperties.text"));
            moduleConfig.addActionListener(action -> {
                showPopupMenu(event.getX(), event.getY(), new ModuleConfigPopup(project, moduleName));
            });
            items.add(moduleConfig);

            JBMenuItem moduleHeaders = new JBMenuItem("Module Headers");
            moduleHeaders.addActionListener(action -> {
                showPopupMenu(event.getX(), event.getY(), new ModuleHeadersPopup(project, moduleName));
            });
            items.add(moduleHeaders);
        }

        if (items.isEmpty()) {
            return null;
        }
        JBPopupMenu menu = new JBPopupMenu();
        items.forEach(menu::add);
        return menu;
    }

    @Override
    protected @Nullable Consumer<BaseNode<?>> getChooseListener() {
        return node -> {
            if (!(node instanceof ServiceNode) || chooseCallback == null) {
                return;
            }
            ServiceNode serviceNode = (ServiceNode) node;
            chooseCallback.accept(serviceNode.getSource());
        };
    }

    @Override
    protected @Nullable Consumer<BaseNode<?>> getDoubleClickListener() {
        return node -> {
            if (node instanceof ServiceNode) {
                ((ServiceNode) node).getSource().navigate(true);
            }
        };
    }

    public void setChooseCallback(@Nullable Consumer<ApiService> chooseCallback) {
        this.chooseCallback = chooseCallback;
    }

    /**
     * 渲染
     */
    public void renderAll(@NotNull BaseNode<?> root, @NotNull Map<PsiMethod, ServiceNode> serviceNodes, @Nullable Boolean expand) {
        apiServiceNodes.clear();
        apiServiceNodes.putAll(serviceNodes);

        super.render(root);

        if (expand != null && expand) {
            treeExpand();
        } else {
            treeCollapse();
        }
    }

    /**
     * 转到tree
     */
    public void navigationToTree(@NotNull PsiMethod psiMethod) {
        if (apiServiceNodes == null || apiServiceNodes.isEmpty()) {
            project.getMessageBus()
                    .syncPublisher(RefreshServiceTreeTopic.TOPIC)
                    .refresh();
            return;
        }
        ServiceNode serviceNode = apiServiceNodes.get(psiMethod);
        if (serviceNode == null) {
            return;
        }
        //有节点到根路径数组
        javax.swing.tree.TreeNode[] nodes = getTreeModel().getPathToRoot(serviceNode);
        TreePath path = new TreePath(nodes);
        getTree().setSelectionPath(path);
    }
}
