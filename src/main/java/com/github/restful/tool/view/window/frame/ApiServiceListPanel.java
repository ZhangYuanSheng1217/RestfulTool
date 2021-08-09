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
import com.github.restful.tool.beans.settings.Settings;
import com.github.restful.tool.service.Notify;
import com.github.restful.tool.service.topic.RefreshServiceTreeTopic;
import com.github.restful.tool.utils.Bundle;
import com.github.restful.tool.utils.SystemUtil;
import com.github.restful.tool.view.components.popups.ModuleConfigPopup;
import com.github.restful.tool.view.window.ApiTreeCellRenderer;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ApiServiceListPanel extends JBScrollPane {

    private final transient Project project;

    /**
     * 树 - service列表
     */
    private final Tree tree;

    private final Map<PsiMethod, ServiceNode> apiServiceNodes;

    @Nullable
    private transient ChooseRequestCallback chooseRequestCallback;

    private transient Map<String, List<ApiService>> apiServices;

    public ApiServiceListPanel(@NotNull Project project) {
        this.project = project;
        this.apiServiceNodes = new HashMap<>();
        this.tree = new SimpleTree();

        this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setBorder(new CustomLineBorder(JBUI.insetsTop(1)));

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.setRoot(new DefaultMutableTreeNode());
        tree.setCellRenderer(new ApiTreeCellRenderer());
        tree.setRootVisible(true);
        tree.setShowsRootHandles(false);
        this.setViewportView(tree);

        initEvent();
    }

    /**
     * 重新渲染列表所有的apiService
     */
    public void renderAll(@NotNull Map<String, List<ApiService>> apiServices) {
        this.apiServices = apiServices;

        TreeNode<String> root = new TreeNode<>(Bundle.getString("service.tree.NotFoundAny"));
        AtomicInteger apiCount = new AtomicInteger();

        apiServiceNodes.clear();
        apiServices.forEach((itemName, requests) -> {
            if (requests == null || requests.isEmpty()) {
                return;
            }
            ModuleNode moduleNode = getModuleNode(itemName, requests);
            apiCount.addAndGet(moduleNode.getApiCount());
            apiServiceNodes.putAll(moduleNode.getServiceNodes());
            root.add(moduleNode);
        });

        ((DefaultTreeModel) tree.getModel()).setRoot(root);

        Boolean expand = Settings.SystemOptionForm.EXPAND_OF_SERVICE_TREE.getData();
        if (expand != null && expand) {
            expandAll(true);
        }

        // api数量小于1才显示根节点
        tree.firePropertyChange(JTree.ROOT_VISIBLE_PROPERTY, tree.isRootVisible(), apiCount.get() < 1);
        // api数量小于1则不可点击
        tree.setEnabled(apiCount.get() > 0);
    }

    @NotNull
    public List<ApiService> getModuleServices(@NotNull String moduleName) {
        if (this.apiServices == null || this.apiServices.isEmpty()) {
            return Collections.emptyList();
        }
        return this.apiServices.getOrDefault(moduleName, Collections.emptyList());
    }

    /**
     * 重新渲染apiService
     */
    public void render(@NotNull String moduleName, @NotNull List<ApiService> apiServices) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        @SuppressWarnings("unchecked")
        TreeNode<String> root = (TreeNode<String>) model.getRoot();
        Enumeration<javax.swing.tree.TreeNode> children = root.children();
        ModuleNode moduleNode = null;
        while (children.hasMoreElements()) {
            javax.swing.tree.TreeNode treeNode = children.nextElement();
            if (treeNode instanceof ModuleNode) {
                ModuleNode node = (ModuleNode) treeNode;
                ModuleTree moduleTree = node.getData();
                if (moduleTree.getModuleName().equals(moduleName)) {
                    moduleNode = node;
                    break;
                }
            }
        }
        if (moduleNode == null) {
            return;
        }
        root.remove(moduleNode);
        // 更新module节点
        moduleNode = getModuleNode(moduleName, apiServices);
        root.add(moduleNode);
    }

    @NotNull
    private ModuleNode getModuleNode(String itemName, List<ApiService> requests) {
        Map<PsiClass, List<ApiService>> collect = new HashMap<>(1);
        Boolean showClass = Settings.SystemOptionForm.SHOW_CLASS_SERVICE_TREE.getData();
        if (showClass != null && showClass) {
            collect = requests.stream().collect(Collectors.toMap(
                    request -> {
                        NavigatablePsiElement psiElement = request.getPsiElement();
                        PsiElement parent = psiElement.getParent();
                        if (parent instanceof PsiClass) {
                            return ((PsiClass) parent);
                        }
                        return null;
                    },
                    request -> new ArrayList<>(Collections.singletonList(request)),
                    (list1, list2) -> {
                        list1.addAll(list2);
                        return list1;
                    }
            ));
        } else {
            collect.put(null, requests);
        }

        return new ModuleNode(new ModuleTree(itemName, requests.size()), collect);
    }

    @NotNull
    public ServiceNode getServiceNode(@NotNull PsiMethod method) {
        return apiServiceNodes.get(method);
    }

    public void setChooseRequestCallback(@Nullable ChooseRequestCallback chooseRequestCallback) {
        this.chooseRequestCallback = chooseRequestCallback;
    }

    private void initEvent() {
        // RequestTree子项点击监听
        tree.addTreeSelectionListener(e -> {
            ApiService apiService = getTreeNodeRequest(tree);
            if (chooseRequestCallback == null) {
                return;
            }
            if (apiService == null) {
                chooseRequestCallback.choose(null);
                return;
            }
            chooseRequestCallback.choose(apiService);
        });

        // RequestTree子项双击监听
        tree.addMouseListener(new MouseListener());

        // 按回车键跳转到对应方法
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ApiService apiService = getTreeNodeRequest(tree);
                    if (apiService != null) {
                        apiService.navigate(true);
                    }
                }
            }
        });
    }

    @Nullable
    private ApiService getTreeNodeRequest(@NotNull JTree tree) {
        DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (mutableTreeNode == null) {
            return null;
        }
        Object userObject = mutableTreeNode.getUserObject();
        if (userObject instanceof ApiService) {
            return (ApiService) userObject;
        }
        return null;
    }

    public void expandAll(boolean expand) {
        expandAll(new TreePath(tree.getModel().getRoot()), expand);
    }

    /**
     * 展开tree视图
     *
     * @param parent treePath
     * @param expand 是否展开
     */
    private void expandAll(@NotNull TreePath parent, boolean expand) {
        TreeNode<?> node = (TreeNode<?>) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
                javax.swing.tree.TreeNode n = (javax.swing.tree.TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(path, expand);
            }
        }

        // 展开或收起必须自下而上进行
        if (expand) {
            tree.expandPath(parent);
        } else {
            if (node.isRoot()) {
                return;
            }
            tree.collapsePath(parent);
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
        javax.swing.tree.TreeNode[] nodes = ((DefaultTreeModel) tree.getModel()).getPathToRoot(serviceNode);
        TreePath path = new TreePath(nodes);
        tree.setSelectionPath(path);
    }

    interface ChooseRequestCallback {

        /**
         * 选择的Request项
         *
         * @param apiService request
         */
        void choose(@Nullable ApiService apiService);
    }

    public static class TreeNode<T> extends DefaultMutableTreeNode {

        private final transient T data;

        public TreeNode(@NotNull T data) {
            super(data);
            this.data = data;
        }

        @NotNull
        public T getData() {
            return data;
        }
    }

    public static class ModuleNode extends TreeNode<ModuleTree> {

        private final AtomicInteger count;
        private final transient Map<PsiMethod, ServiceNode> serviceNodes;

        public ModuleNode(@NotNull ModuleTree data, Map<PsiClass, List<ApiService>> collect) {
            super(data);
            this.count = new AtomicInteger();
            this.serviceNodes = new HashMap<>();

            if (collect == null || collect.isEmpty()) {
                return;
            }
            collect.forEach((psiClass, items) -> {
                if (psiClass != null) {
                    ClassNode node = new ClassNode(new ClassTree(psiClass));
                    items.forEach(request -> {
                        ServiceNode serviceNode = new ServiceNode(request);
                        if (request.getPsiElement() instanceof PsiMethod) {
                            serviceNodes.put((PsiMethod) request.getPsiElement(), serviceNode);
                        }
                        node.add(serviceNode);
                        count.incrementAndGet();
                    });
                    this.add(node);
                } else {
                    items.forEach(request -> {
                        ServiceNode serviceNode = new ServiceNode(request);
                        if (request.getPsiElement() instanceof PsiMethod) {
                            serviceNodes.put((PsiMethod) request.getPsiElement(), serviceNode);
                        }
                        this.add(serviceNode);
                        count.incrementAndGet();
                    });
                }
            });
        }

        @NotNull
        public final Integer getApiCount() {
            return count.get();
        }

        @NotNull
        public final Map<PsiMethod, ServiceNode> getServiceNodes() {
            return serviceNodes;
        }
    }

    public static class ClassNode extends TreeNode<ClassTree> {

        public ClassNode(@NotNull ClassTree data) {
            super(data);
        }
    }

    public static class ServiceNode extends TreeNode<ApiService> {

        public ServiceNode(@NotNull ApiService data) {
            super(data);
        }
    }

    private class MouseListener extends MouseAdapter {

        private JPopupMenu modulePopupMenu;
        private JPopupMenu classPopupMenu;
        private JPopupMenu requestItemPopupMenu;

        @Nullable
        private ClassTree getTreeNodeClassTree(@NotNull JTree tree) {
            DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (mutableTreeNode == null) {
                return null;
            }
            Object userObject = mutableTreeNode.getUserObject();
            if (!(userObject instanceof ClassTree)) {
                return null;
            }
            return (ClassTree) userObject;
        }

        @NotNull
        private JPopupMenu generatePopupMenu(JComponent... items) {
            JBPopupMenu menu = new JBPopupMenu();
            if (items == null) {
                return menu;
            }
            for (int i = 0; i < items.length; i++) {
                JComponent item = items[i];
                if (item != null) {
                    if (item instanceof JMenuItem) {
                        ((JMenuItem) item).setMnemonic(i);
                    }
                    menu.add(item);
                } else {
                    menu.addSeparator();
                }
            }
            return menu;
        }

        /**
         * 显示右键菜单
         */
        private void showPopupMenu(@NotNull MouseEvent event, @Nullable JPopupMenu menu) {
            if (menu == null) {
                return;
            }
            TreePath path = tree.getPathForLocation(event.getX(), event.getY());
            tree.setSelectionPath(path);
            Rectangle rectangle = tree.getUI().getPathBounds(tree, path);
            if (rectangle != null && rectangle.contains(event.getX(), event.getY())) {
                menu.show(tree, event.getX(), rectangle.y + rectangle.height);
            }
        }

        @Nullable
        private ModuleTree getTreeNodeModuleTree(@NotNull JTree tree) {
            DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (mutableTreeNode == null) {
                return null;
            }
            Object userObject = mutableTreeNode.getUserObject();
            if (!(userObject instanceof ModuleTree)) {
                return null;
            }
            return (ModuleTree) userObject;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            final int doubleClick = 2;
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() > 0 && e.getClickCount() % doubleClick == 0) {
                ApiService node = getTreeNodeRequest(tree);
                if (node != null && e.getClickCount() == doubleClick) {
                    node.navigate(true);
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                ApiService apiService = getTreeNodeRequest(tree);
                if (apiService != null) {
                    showPopupMenu(e, getRequestItemPopupMenu());
                    return;
                }

                ClassTree classTree = getTreeNodeClassTree(tree);
                if (classTree != null) {
                    showPopupMenu(e, getClassPopupMenu());
                }

                ModuleTree moduleTree = getTreeNodeModuleTree(tree);
                if (moduleTree != null) {
                    showPopupMenu(e, getModulePopupMenu(e));
                }
            }
        }

        private JPopupMenu getClassPopupMenu() {
            if (classPopupMenu == null) {
                // navigation
                JMenuItem navigation = new JBMenuItem(Bundle.getString("action.NavigateToClass.text"), AllIcons.Nodes.Class);
                navigation.addActionListener(actionEvent -> {
                    ClassTree classTree = getTreeNodeClassTree(tree);
                    if (classTree == null) {
                        return;
                    }
                    classTree.getPsiClass().navigate(true);
                });
                classPopupMenu = generatePopupMenu(navigation);
            }
            return classPopupMenu;
        }

        private JPopupMenu getModulePopupMenu(MouseEvent event) {
            ModuleTree moduleTree = getTreeNodeModuleTree(tree);
            if (moduleTree == null) {
                return null;
            }
            if (modulePopupMenu == null) {
                final String moduleName = moduleTree.getModuleName();

                JBMenuItem moduleSetting = new JBMenuItem(Bundle.getString("action.OpenModuleSetting.text"), AllIcons.General.Settings);
                moduleSetting.addActionListener(action -> {
                    Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
                    if (module == null) {
                        return;
                    }
                    // 打开当前项目模块设置
                    ProjectSettingsService.getInstance(project).openModuleSettings(module);
                });

                JBMenuItem moduleConfig = new JBMenuItem(Bundle.getString("action.OpenModuleProperties.text"));
                moduleConfig.addActionListener(action -> {
                    ModuleConfigPopup menu = new ModuleConfigPopup(project, moduleName);
                    showPopupMenu(event, menu);
                });

                modulePopupMenu = generatePopupMenu(moduleSetting, moduleConfig);
            }
            return modulePopupMenu;
        }

        private JPopupMenu getRequestItemPopupMenu() {
            if (requestItemPopupMenu == null) {

                // navigation
                JMenuItem navigation = new JBMenuItem(Bundle.getString("action.NavigateToMethod.text"), AllIcons.Nodes.Method);
                navigation.addActionListener(actionEvent -> {
                    ApiService apiService = getTreeNodeRequest(tree);
                    if (apiService == null) {
                        return;
                    }
                    apiService.navigate(true);
                });

                // Copy full url
                JMenuItem copyFullUrl = new JBMenuItem(Bundle.getString("action.CopyFullPath.text"), AllIcons.Actions.Copy);
                copyFullUrl.addActionListener(actionEvent -> {
                    ApiService apiService = getTreeNodeRequest(tree);
                    if (apiService == null) {
                        return;
                    }
                    SystemUtil.Clipboard.copy(apiService.getRequestUrl());
                    Notify.getInstance(project).info(Bundle.getString("action.CopyFullPath.success"));
                });

                // Copy api path
                JMenuItem copyApiPath = new JBMenuItem(Bundle.getString("action.CopyApi.text"), AllIcons.Actions.Copy);
                copyApiPath.addActionListener(actionEvent -> {
                    ApiService apiService = getTreeNodeRequest(tree);
                    if (apiService == null) {
                        return;
                    }
                    SystemUtil.Clipboard.copy(apiService.getPath());
                    Notify.getInstance(project).info(Bundle.getString("action.CopyApi.success"));
                });
                requestItemPopupMenu = generatePopupMenu(
                        navigation,
                        null,
                        copyFullUrl, copyApiPath
                );
            }
            return requestItemPopupMenu;
        }
    }
}
