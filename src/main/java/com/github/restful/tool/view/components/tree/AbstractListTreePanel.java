package com.github.restful.tool.view.components.tree;

import com.intellij.ide.TreeExpander;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.function.Consumer;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public abstract class AbstractListTreePanel extends JBScrollPane implements TreeExpander {

    private final JTree tree;

    public AbstractListTreePanel(@NotNull final JTree tree) {
        this.tree = tree;

        this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setBorder(new CustomLineBorder(JBUI.insetsTop(1)));

        this.tree.setCellRenderer(new CustomTreeCellRenderer());
        this.tree.setRootVisible(true);
        this.tree.setShowsRootHandles(false);
        this.setViewportView(tree);

        this.tree.addTreeSelectionListener(e -> {
            if (!this.tree.isEnabled()) {
                return;
            }
            Object component = tree.getLastSelectedPathComponent();
            if (!(component instanceof BaseNode<?>)) {
                return;
            }
            BaseNode<?> node = (BaseNode<?>) component;
            if (getChooseListener() != null) {
                getChooseListener().accept(node);
            }
        });
        this.tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (!tree.isEnabled()) {
                    return;
                }
                BaseNode<?> node = getNode(event);
                if (node == null) {
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(event)) {
                    if (event.getClickCount() == 2 && getDoubleClickListener() != null) {
                        getDoubleClickListener().accept(node);
                    }
                } else if (SwingUtilities.isRightMouseButton(event)) {
                    showPopupMenu(event.getX(), event.getY(), getPopupMenu(event, node));
                }
            }

            @Nullable
            private BaseNode<?> getNode(@NotNull MouseEvent event) {
                TreePath path = tree.getPathForLocation(event.getX(), event.getY());
                tree.setSelectionPath(path);
                return getChooseNode(path);
            }
        });
    }

    protected final JTree getTree() {
        return this.tree;
    }

    protected final DefaultTreeModel getTreeModel() {
        return (DefaultTreeModel) this.tree.getModel();
    }

    public final void render(@NotNull BaseNode<?> rootNode) {
        getTreeModel().setRoot(rootNode);
    }

    @Nullable
    public BaseNode<?> getChooseNode(@Nullable TreePath treePath) {
        Object component = null;
        if (treePath != null) {
            component = treePath.getLastPathComponent();
        } else {
            component = tree.getLastSelectedPathComponent();
        }
        if (!(component instanceof BaseNode<?>)) {
            return null;
        }
        return (BaseNode<?>) component;
    }

    public void treeExpand() {
        expandAll(new TreePath(tree.getModel().getRoot()), true);
    }

    public void treeCollapse() {
        expandAll(new TreePath(tree.getModel().getRoot()), false);
    }

    /**
     * 展开tree视图
     *
     * @param parent treePath
     * @param expand 是否展开
     */
    private void expandAll(@NotNull TreePath parent, boolean expand) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
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
     * 显示右键菜单
     */
    protected void showPopupMenu(int x, int y, @Nullable JPopupMenu menu) {
        if (menu == null) {
            return;
        }
        TreePath path = tree.getPathForLocation(x, y);
        tree.setSelectionPath(path);
        Rectangle rectangle = tree.getUI().getPathBounds(tree, path);
        if (rectangle != null && rectangle.contains(x, y)) {
            menu.show(tree, x, rectangle.y + rectangle.height);
        }
    }

    @Override
    public boolean canExpand() {
        return tree.getRowCount() > 0;
    }

    @Override
    public boolean canCollapse() {
        return tree.getRowCount() > 0;
    }

    @Override
    public void expandAll() {
        expandAll(new TreePath(tree.getModel().getRoot()), true);
    }

    @Override
    public void collapseAll() {
        expandAll(new TreePath(tree.getModel().getRoot()), false);
    }

    @Nullable
    protected abstract JPopupMenu getPopupMenu(@NotNull MouseEvent event, @NotNull BaseNode<?> node);

    @Nullable
    protected abstract Consumer<BaseNode<?>> getChooseListener();

    @Nullable
    protected abstract Consumer<BaseNode<?>> getDoubleClickListener();
}
