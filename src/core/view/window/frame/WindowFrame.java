package core.view.window.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import core.beans.Request;
import core.utils.RestUtil;
import core.view.window.RestfulTreeCellRenderer;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTree;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZhangYuanSheng
 */
public class WindowFrame extends JPanel {

    /**
     * 项目对象
     */
    private final Project project;
    private final RestDetail restDetail;

    /**
     * 按钮 - 扫描service
     */
    private JButton scanApi;
    /**
     * 树 - service列表
     */
    private JTree tree;

    /**
     * Create the panel.
     */
    public WindowFrame(@NotNull Project project) {
        this.project = project;
        this.restDetail = new RestDetail(project);

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        JPanel headPanel = new JPanel();
        GridBagConstraints gbcHeadPanel = new GridBagConstraints();
        gbcHeadPanel.weighty = 2.5;
        gbcHeadPanel.insets = JBUI.insetsBottom(5);
        gbcHeadPanel.fill = GridBagConstraints.BOTH;
        gbcHeadPanel.gridx = 0;
        gbcHeadPanel.gridy = 0;
        add(headPanel, gbcHeadPanel);
        headPanel.setLayout(new BorderLayout(0, 0));

        initView(headPanel);

        GridBagConstraints gbcBodyPanel = new GridBagConstraints();
        gbcBodyPanel.weighty = 1.0;
        gbcBodyPanel.fill = GridBagConstraints.BOTH;
        gbcBodyPanel.gridx = 0;
        gbcBodyPanel.gridy = 1;
        add(restDetail, gbcBodyPanel);

        initEvent();

        firstLoad();
    }

    private void initView(@NotNull JPanel headPanel) {
        JPanel toolPanel = new JPanel();
        headPanel.add(toolPanel, BorderLayout.NORTH);
        toolPanel.setLayout(new BorderLayout(0, 0));

        scanApi = new JXButton(AllIcons.Actions.Refresh);
        Dimension scanApiSize = new Dimension(24, 24);
        scanApi.setPreferredSize(scanApiSize);
        // 按钮设置为透明，这样就不会挡着后面的背景
        scanApi.setContentAreaFilled(true);
        // 去掉按钮的边框
        scanApi.setBorderPainted(false);
        toolPanel.add(scanApi, BorderLayout.WEST);

        JScrollPane scrollPaneTree = new JBScrollPane();
        scrollPaneTree.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        headPanel.add(scrollPaneTree, BorderLayout.CENTER);

        tree = new JXTree();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.setRoot(new DefaultMutableTreeNode());
        tree.setCellRenderer(new RestfulTreeCellRenderer());
        tree.setRootVisible(true);
        tree.setShowsRootHandles(false);
        scrollPaneTree.setViewportView(tree);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        // 控制器扫描监听
        scanApi.addActionListener(e -> renderRequestTree());

        // RequestTree子项点击监听
        tree.addTreeSelectionListener(e -> {
            Request node = getTreeNodeRequest(tree);
            if (node == null) {
                return;
            }
            restDetail.setRequest(node);
        });

        // RequestTree子项双击监听
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Request node = getTreeNodeRequest(tree);
                if (node != null && e.getClickCount() == 2) {
                    node.navigate(true);
                }
            }
        });
    }

    private void firstLoad() {
        try {
            /*
            TODO Cannot use JavaAnnotationIndex to scan Spring annotations when the project is not IDE index
             项目未被 IDE index时，无法使用JavaAnnotationIndex扫描Spring注解
             solution:
              1. Tool Window is dynamically registered when the project index is completed.
                 待项目index完毕时才动态注册ToolWindow窗口
              2. If the project is not indexed, stop scanning service, add callback when index is completed, scan and render service tree.
                 项目未被index则停止扫描service，增加index完毕回调，扫描渲染serviceTree
             */
            renderRequestTree();
        } catch (Exception e) {
            DumbService.getInstance(project).showDumbModeNotification(
                    "The project has not been loaded yet. Please wait until the project is loaded and try again."
            );
        }
    }

    /**
     * 渲染Restful请求列表
     */
    private void renderRequestTree() {
        AtomicInteger controllerCount = new AtomicInteger();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(controllerCount.get());

        Map<String, List<Request>> allRequest = RestUtil.getAllRequest(project);
        allRequest.forEach((moduleName, requests) -> {
            DefaultMutableTreeNode item = new DefaultMutableTreeNode(String.format(
                    "[%d]%s",
                    requests.size(),
                    moduleName
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
        expandAll(tree, new TreePath(tree.getModel().getRoot()), true);
    }

    @Nullable
    private Request getTreeNodeRequest(@NotNull JTree tree) {
        DefaultMutableTreeNode sel = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (sel == null) {
            return null;
        }
        Object object = sel.getUserObject();
        if (!(object instanceof Request)) {
            return null;
        }
        return (Request) object;
    }

    private void expandAll(JTree tree, @NotNull TreePath parent, boolean expand) {
        javax.swing.tree.TreeNode node = (javax.swing.tree.TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
                javax.swing.tree.TreeNode n = (javax.swing.tree.TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // 展开或收起必须自下而上进行
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }
}
