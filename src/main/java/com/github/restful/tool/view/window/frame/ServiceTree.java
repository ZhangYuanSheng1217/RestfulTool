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

import com.github.restful.tool.beans.ClassTree;
import com.github.restful.tool.beans.ModuleTree;
import com.github.restful.tool.beans.Request;
import com.github.restful.tool.beans.settings.Settings;
import com.github.restful.tool.service.Notify;
import com.github.restful.tool.utils.Bundle;
import com.github.restful.tool.utils.RestUtil;
import com.github.restful.tool.utils.SystemUtil;
import com.github.restful.tool.view.window.RestfulTreeCellRenderer;
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
import com.intellij.psi.search.GlobalSearchScope;
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
public class ServiceTree extends JBScrollPane {

    private final Project project;

    /**
     * 树 - service列表
     */
    private final Tree tree;

    private final Map<PsiMethod, RequestNode> requestNodeMap;

    @Nullable
    private ChooseRequestCallback chooseRequestCallback;

    public ServiceTree(@NotNull Project project) {
        this.project = project;
        this.requestNodeMap = new HashMap<>();
        tree = new SimpleTree();

        this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setBorder(new CustomLineBorder(JBUI.insetsTop(1)));

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.setRoot(new DefaultMutableTreeNode());
        tree.setCellRenderer(new RestfulTreeCellRenderer());
        tree.setRootVisible(true);
        tree.setShowsRootHandles(false);
        this.setViewportView(tree);

        initEvent();
    }

    /**
     * 渲染Restful请求列表
     */
    public void renderRequestTree(@NotNull Map<String, List<Request>> allRequests) {
        AtomicInteger apiCount = new AtomicInteger();
        TreeNode<String> root = new TreeNode<>(Bundle.getString("service.tree.NotFoundAny"));

        requestNodeMap.clear();
        allRequests.forEach((itemName, requests) -> {
            if (requests == null || requests.isEmpty()) {
                return;
            }
            Map<PsiClass, List<Request>> collect = new HashMap<>(1);
            if (Settings.SystemOptionForm.SHOW_CLASS_SERVICE_TREE.getData()) {
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

            ModuleNode moduleNode = new ModuleNode(new ModuleTree(itemName, requests.size()));
            collect.forEach((psiClass, items) -> {
                if (psiClass != null) {
                    ControllerNode node = new ControllerNode(new ClassTree(psiClass));
                    items.forEach(request -> {
                        RequestNode requestNode = new RequestNode(request);
                        if (request.getPsiElement() instanceof PsiMethod) {
                            requestNodeMap.put((PsiMethod) request.getPsiElement(), requestNode);
                        }
                        node.add(requestNode);
                        apiCount.incrementAndGet();
                    });
                    moduleNode.add(node);
                } else {
                    items.forEach(request -> {
                        RequestNode requestNode = new RequestNode(request);
                        if (request.getPsiElement() instanceof PsiMethod) {
                            requestNodeMap.put((PsiMethod) request.getPsiElement(), requestNode);
                        }
                        moduleNode.add(requestNode);
                        apiCount.incrementAndGet();
                    });
                }
            });
            root.add(moduleNode);
        });

        ((DefaultTreeModel) tree.getModel()).setRoot(root);

        if (Settings.SystemOptionForm.EXPAND_OF_SERVICE_TREE.getData()) {
            expandAll(new TreePath(tree.getModel().getRoot()), true);
        }

        // api数量小于1才显示根节点
        tree.firePropertyChange(JTree.ROOT_VISIBLE_PROPERTY, tree.isRootVisible(), apiCount.get() < 1);
        // api数量小于1则不可点击
        tree.setEnabled(apiCount.get() > 0);
    }

    public void setChooseRequestCallback(@Nullable ChooseRequestCallback chooseRequestCallback) {
        this.chooseRequestCallback = chooseRequestCallback;
    }

    private void initEvent() {
        // RequestTree子项点击监听
        tree.addTreeSelectionListener(e -> {
            Request request = getTreeNodeRequest(tree);
            if (chooseRequestCallback == null) {
                return;
            }
            if (request == null) {
                chooseRequestCallback.choose(null);
                return;
            }
            chooseRequestCallback.choose(request);
        });

        // RequestTree子项双击监听
        tree.addMouseListener(new MouseAdapter() {

            private JPopupMenu modulePopupMenu;
            private JPopupMenu classPopupMenu;
            private JPopupMenu requestItemPopupMenu;

            @Override
            public void mouseClicked(MouseEvent e) {
                final int doubleClick = 2;
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() > 0 && e.getClickCount() % doubleClick == 0) {
                    Request node = getTreeNodeRequest(tree);
                    if (node != null && e.getClickCount() == doubleClick) {
                        node.navigate(true);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Request request = getTreeNodeRequest(tree);
                    if (request != null) {
                        showPopupMenu(e, getRequestItemPopupMenu());
                        return;
                    }

                    ClassTree classTree = getTreeNodeClassTree(tree);
                    if (classTree != null) {
                        showPopupMenu(e, getClassPopupMenu());
                    }

                    ModuleTree moduleTree = getTreeNodeModuleTree(tree);
                    if (moduleTree != null) {
                        showPopupMenu(e, getModulePopupMenu());
                    }
                }
            }

            private JPopupMenu getClassPopupMenu() {
                if (classPopupMenu != null) {
                    return classPopupMenu;
                }
                // navigation
                JMenuItem navigation = new JBMenuItem(Bundle.getString("action.NavigateToClass.text"), AllIcons.Nodes.Class);
                navigation.addActionListener(actionEvent -> {
                    ClassTree classTree = getTreeNodeClassTree(tree);
                    if (classTree == null) {
                        return;
                    }
                    classTree.getPsiClass().navigate(true);
                });
                return (classPopupMenu = generatePopupMenu(navigation));
            }

            private JPopupMenu getModulePopupMenu() {
                ModuleTree moduleTree = getTreeNodeModuleTree(tree);
                if (moduleTree == null) {
                    return null;
                }
                if (modulePopupMenu != null) {
                    return modulePopupMenu;
                }
                JBMenuItem moduleSetting = new JBMenuItem(Bundle.getString("action.OpenModuleSetting.text"), AllIcons.General.Settings);
                moduleSetting.addActionListener(action -> {
                    Module module = ModuleManager.getInstance(project).findModuleByName(moduleTree.getModuleName());
                    if (module == null) {
                        return;
                    }
                    // 打开当前项目模块设置
                    ProjectSettingsService.getInstance(project).openModuleSettings(module);
                });
                return (modulePopupMenu = generatePopupMenu(moduleSetting));
            }

            private JPopupMenu getRequestItemPopupMenu() {
                if (requestItemPopupMenu != null) {
                    return requestItemPopupMenu;
                }

                // navigation
                JMenuItem navigation = new JBMenuItem(Bundle.getString("action.NavigateToMethod.text"), AllIcons.Nodes.Method);
                navigation.addActionListener(actionEvent -> {
                    Request request = getTreeNodeRequest(tree);
                    if (request == null) {
                        return;
                    }
                    request.navigate(true);
                });

                // Copy full url
                JMenuItem copyFullUrl = new JBMenuItem(Bundle.getString("action.CopyFullPath.text"), AllIcons.Actions.Copy);
                copyFullUrl.addActionListener(actionEvent -> {
                    Request request = getTreeNodeRequest(tree);
                    if (request == null) {
                        return;
                    }
                    GlobalSearchScope scope = request.getPsiElement().getResolveScope();
                    String contextPath = RestUtil.scanContextPath(project, scope);
                    SystemUtil.Clipboard.copy(SystemUtil.buildUrl(
                            RestUtil.scanListenerProtocol(project, scope),
                            RestUtil.scanListenerPort(project, scope),
                            contextPath,
                            request.getPath()));
                    Notify.getInstance(project).info(Bundle.getString("action.CopyFullPath.success"));
                });

                // Copy api path
                JMenuItem copyApiPath = new JBMenuItem(Bundle.getString("action.CopyApi.text"), AllIcons.Actions.Copy);
                copyApiPath.addActionListener(actionEvent -> {
                    Request request = getTreeNodeRequest(tree);
                    if (request == null) {
                        return;
                    }
                    GlobalSearchScope scope = request.getPsiElement().getResolveScope();
                    String contextPath = RestUtil.scanContextPath(project, scope);
                    SystemUtil.Clipboard.copy(
                            (contextPath == null || "null".equals(contextPath) ? "" : contextPath) +
                                    request.getPath()
                    );
                    Notify.getInstance(project).info(Bundle.getString("action.CopyApi.success"));
                });
                return (requestItemPopupMenu = generatePopupMenu(
                        navigation,
                        null,
                        copyFullUrl, copyApiPath
                ));
            }
        });

        // 按回车键跳转到对应方法
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Request request = getTreeNodeRequest(tree);
                    if (request != null) {
                        request.navigate(true);
                    }
                }
            }
        });
    }

    @Nullable
    private Request getTreeNodeRequest(@NotNull JTree tree) {
        DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (mutableTreeNode == null) {
            return null;
        }
        Object userObject = mutableTreeNode.getUserObject();
        if (userObject instanceof Request) {
            return (Request) userObject;
        }
        return null;
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

    /**
     * 展开tree视图
     *
     * @param parent treePath
     * @param expand 是否展开
     */
    private void expandAll(@NotNull TreePath parent, boolean expand) {
        javax.swing.tree.TreeNode node = (javax.swing.tree.TreeNode) parent.getLastPathComponent();
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
            tree.collapsePath(parent);
        }
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

    /**
     * 转到tree
     */
    public void navigationToTree(@NotNull PsiMethod psiMethod) {
        RequestNode requestNode = requestNodeMap.get(psiMethod);
        if (requestNode == null) {
            return;
        }
        //有节点到根路径数组
        javax.swing.tree.TreeNode[] nodes = ((DefaultTreeModel) tree.getModel()).getPathToRoot(requestNode);
        TreePath path = new TreePath(nodes);
        tree.setSelectionPath(path);
    }

    interface ChooseRequestCallback {

        /**
         * 选择的Request项
         *
         * @param request request
         */
        void choose(@Nullable Request request);
    }

    public static class TreeNode<T> extends DefaultMutableTreeNode {

        private final T data;

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

        public ModuleNode(@NotNull ModuleTree data) {
            super(data);
        }
    }

    public static class ControllerNode extends TreeNode<ClassTree> {

        public ControllerNode(@NotNull ClassTree data) {
            super(data);
        }
    }

    public static class RequestNode extends TreeNode<Request> {

        public RequestNode(@NotNull Request data) {
            super(data);
        }
    }
}
