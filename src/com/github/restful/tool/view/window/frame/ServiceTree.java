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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.TreeSpeedSearch;
import com.github.restful.tool.beans.Request;
import com.github.restful.tool.service.Notify;
import com.github.restful.tool.utils.RestUtil;
import com.github.restful.tool.utils.SystemUtil;
import com.github.restful.tool.view.window.RestfulTreeCellRenderer;
import org.jdesktop.swingx.JXTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ServiceTree extends JScrollPane {

    private final Project project;

    /**
     * 树 - service列表
     */
    private final JTree tree;

    @Nullable
    private ChooseRequestCallback chooseRequestCallback;

    private boolean showPopupMenu = false;

    public ServiceTree(@NotNull Project project) {
        this.project = project;
        tree = new JXTree();

        this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.setRoot(new DefaultMutableTreeNode());
        tree.setCellRenderer(new RestfulTreeCellRenderer());
        tree.setRootVisible(true);
        tree.setShowsRootHandles(false);
        this.setViewportView(tree);

        // 快速搜索
        new TreeSpeedSearch(tree);

        initEvent();
    }

    /**
     * 渲染Restful请求列表
     */
    public void renderRequestTree(@NotNull Map<String, List<Request>> allRequests) {
        AtomicInteger controllerCount = new AtomicInteger();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(controllerCount.get());

        allRequests.forEach((itemName, requests) -> {
            DefaultMutableTreeNode item = new DefaultMutableTreeNode(String.format(
                    "[%d]%s",
                    requests.size(),
                    itemName
            ));
            requests.forEach(request -> {
                item.add(new DefaultMutableTreeNode(request));
                controllerCount.incrementAndGet();
            });
            root.add(item);
        });

        root.setUserObject(controllerCount.get());
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.setRoot(root);
        expandAll(new TreePath(tree.getModel().getRoot()), true);
    }

    public void setChooseRequestCallback(@Nullable ChooseRequestCallback chooseRequestCallback) {
        this.chooseRequestCallback = chooseRequestCallback;
    }

    public void showPopupMenu() {
        this.showPopupMenu = true;
    }

    public void hidePopupMenu() {
        this.showPopupMenu = false;
    }

    private void initEvent() {
        // RequestTree子项点击监听
        tree.addTreeSelectionListener(e -> {
            Request request = getTreeNodeRequest(tree);
            if (request == null || chooseRequestCallback == null) {
                return;
            }
            // restDetail.setRequest(request);
            chooseRequestCallback.choose(request);
        });

        // RequestTree子项双击监听
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    final int doubleClick = 2;
                    Request node = getTreeNodeRequest(tree);
                    if (node != null && e.getClickCount() == doubleClick) {
                        node.navigate(true);
                    }
                }
            }

            /**
             * 右键菜单
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                if (showPopupMenu && SwingUtilities.isRightMouseButton(e)) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    tree.setSelectionPath(path);

                    Request request = getTreeNodeRequest(tree);
                    if (request == null) {
                        return;
                    }

                    Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);
                    if (pathBounds != null && pathBounds.contains(e.getX(), e.getY())) {
                        popupMenu(tree, request, e.getX(), pathBounds.y + pathBounds.height);
                    }
                }
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
            return ((Request) userObject);
        }
        return null;
    }

    /**
     * 显示右键菜单
     *
     * @param tree    tree
     * @param request request
     * @param x       横坐标
     * @param y       纵坐标
     */
    private void popupMenu(@NotNull JTree tree, @NotNull Request request, int x, int y) {
        JBPopupMenu menu = new JBPopupMenu();
        ActionListener actionListener = actionEvent -> {
            String copy;
            GlobalSearchScope scope = request.getPsiMethod().getResolveScope();
            String contextPath = RestUtil.scanContextPath(project, scope);
            switch (((JMenuItem) actionEvent.getSource()).getMnemonic()) {
                case 0:
                    copy = SystemUtil.buildUrl(
                            RestUtil.scanListenerProtocol(project, scope),
                            RestUtil.scanListenerPort(project, scope),
                            contextPath,
                            request.getPath()
                    );
                    break;
                case 1:
                    copy = (contextPath == null || "null".equals(contextPath) ? "" : contextPath) +
                            request.getPath();
                    break;
                default:
                    return;
            }
            SystemUtil.Clipboard.copy(copy);
            Notify.getInstance(project).info("Copy path success.");
        };

        // Copy full url
        JMenuItem copyFullUrl = new JMenuItem("Copy full url", AllIcons.Actions.Copy);
        copyFullUrl.setMnemonic(0);
        copyFullUrl.addActionListener(actionListener);
        menu.add(copyFullUrl);

        // Copy api path
        JMenuItem copyApiPath = new JMenuItem("Copy api path", AllIcons.Actions.Copy);
        copyApiPath.setMnemonic(1);
        copyApiPath.addActionListener(actionListener);
        menu.add(copyApiPath);

        menu.show(tree, x, y);
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

    interface ChooseRequestCallback {

        /**
         * 选择的Request项
         *
         * @param request request
         */
        void choose(@NotNull Request request);
    }
}
