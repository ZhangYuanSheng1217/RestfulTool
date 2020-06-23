package core.view.window.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import core.beans.HttpMethod;
import core.beans.PropertiesKey;
import core.beans.Request;
import core.service.Notify;
import core.service.topic.RefreshServiceTreeTopic;
import core.service.topic.ServiceTreeTopic;
import core.utils.RestUtil;
import core.utils.SystemUtil;
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
import java.awt.event.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZhangYuanSheng
 */
public class RightToolWindow extends JSplitPane {

    private static final Map<HttpMethod, Boolean> METHOD_CHOOSE_MAP;
    private static final double WINDOW_WEIGHT = 0.55D;

    static {
        HttpMethod[] values = HttpMethod.values();
        METHOD_CHOOSE_MAP = new HashMap<>(values.length);
        for (HttpMethod value : values) {
            METHOD_CHOOSE_MAP.put(value, true);
        }
    }

    /**
     * 项目对象
     */
    private final Project project;
    private final RestDetail restDetail;
    private final Dimension toolBarDimension = new Dimension(24, 24);
    /**
     * 按钮 - 扫描service
     */
    private JButton scanApi;
    /**
     * 按钮 - 扫描service的过滤器
     */
    private JButton scanApiFilter;
    /**
     * 单选框 - 扫描service时是否包含lib
     */
    private JCheckBox scanWithLibrary;
    /**
     * 树 - service列表
     */
    private JTree tree;

    /**
     * Create the panel.
     */
    public RightToolWindow(@NotNull Project project) {
        super(VERTICAL_SPLIT);

        this.project = project;
        this.restDetail = new RestDetail(project);
        this.restDetail.setCallback(this::renderRequestTree);

        setContinuousLayout(true);
        setResizeWeight(WINDOW_WEIGHT);
        setDividerSize(2);
        setBorder(JBUI.Borders.empty());

        JPanel headPanel = new JPanel(new BorderLayout());
        initView(headPanel);
        setTopComponent(headPanel);

        setBottomComponent(restDetail);

        initEvent();

        DumbService.getInstance(project).smartInvokeLater(this::firstLoad);
    }

    private void initView(@NotNull JPanel headPanel) {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headPanel.add(toolBar, BorderLayout.NORTH);

        scanApi = new JXButton(AllIcons.Actions.Refresh);
        scanApi.setToolTipText("Refresh");
        // 按钮设置为透明，这样就不会挡着后面的背景
        scanApi.setContentAreaFilled(true);
        // 去掉按钮的边框
        scanApi.setBorderPainted(false);
        toolBar.add(scanApi);

        toolBar.add(new JBLabel("|"));

        scanApiFilter = new JXButton(AllIcons.General.Filter);
        scanApiFilter.setToolTipText("Method filter");
        scanApiFilter.setContentAreaFilled(true);
        scanApiFilter.setBorderPainted(false);
        toolBar.add(scanApiFilter);

        toolBar.add(new JBLabel("|"));

        scanWithLibrary = new JBCheckBox("withLibrary");
        scanWithLibrary.setToolTipText("Scan service with library");
        scanWithLibrary.setSelected(PropertiesKey.scanServiceWithLibrary(project));
        toolBar.add(scanWithLibrary);

        setComponentDimension(toolBarDimension, toolBar, scanApi, scanApiFilter);

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

        // 快速搜索
        new TreeSpeedSearch(tree);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        // 控制器扫描监听
        scanApi.addActionListener(e -> renderRequestTree());

        scanWithLibrary.addActionListener(e -> {
            PropertiesKey.scanServiceWithLibrary(project, scanWithLibrary.isSelected());
            renderRequestTree();
        });

        HttpMethodFilterPopup<HttpMethod> filterPopup = new HttpMethodFilterPopup<>(HttpMethod.values());
        filterPopup.setChangeCallback((checkBox, method) -> DumbService.getInstance(project).runWhenSmart(() -> {
            METHOD_CHOOSE_MAP.put(method, checkBox.isSelected());
            scanApi.doClick();
        }));
        filterPopup.setChangeAllCallback((ts, selected) -> DumbService.getInstance(project).runWhenSmart(() -> {
            for (HttpMethod method : ts) {
                METHOD_CHOOSE_MAP.put(method, selected);
            }
            scanApi.doClick();
        }));
        scanApiFilter.addActionListener(e -> filterPopup.show(this, 0, toolBarDimension.height));

        project.getMessageBus().connect().subscribe(ServiceTreeTopic.TOPIC, this::renderRequestTree);
        project.getMessageBus().connect().subscribe(RefreshServiceTreeTopic.TOPIC, this::renderRequestTree);

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
                if (SwingUtilities.isRightMouseButton(e)) {
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

    private void firstLoad() {
        renderRequestTree();
    }

    @NotNull
    private Map<String, List<Request>> getRequests() {
        Map<String, List<Request>> allRequest = RestUtil.getAllRequest(project);

        allRequest.forEach((moduleName, requests) -> requests.removeIf(next -> !METHOD_CHOOSE_MAP.get(next.getMethod())));

        return allRequest;
    }

    public void renderRequestTree() {
        ServiceTreeTopic restTopic = project.getMessageBus().syncPublisher(ServiceTreeTopic.TOPIC);
        DumbService.getInstance(project).runWhenSmart(() -> restTopic.action(getRequests()));
    }

    /**
     * 渲染Restful请求列表
     */
    public void renderRequestTree(@NotNull Map<String, List<Request>> allRequest) {
        AtomicInteger controllerCount = new AtomicInteger();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(controllerCount.get());

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

    /**
     * 展开tree视图
     *
     * @param tree   JTree
     * @param parent treePath
     * @param expand 是否展开
     */
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
                    copy = RestUtil.getRequestUrl(
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
            SystemUtil.setClipboardString(copy);
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

    private void setComponentDimension(@NotNull Dimension dimension, @NotNull JComponent... components) {
        for (JComponent component : components) {
            if (component != null) {
                component.setPreferredSize(dimension);
                component.setMaximumSize(dimension);
                component.setMinimumSize(dimension);
                component.setBorder(JBUI.Borders.empty());
            }
        }
    }
}
