package core.view.window.frame;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import core.beans.Request;
import core.beans.RequestMethod;
import core.utils.RestUtil;
import core.view.window.RestfulTreeCellRenderer;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXEditorPane;
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

    /**
     * 按钮 - 扫描service
     */
    private JXButton scanApi;
    /**
     * 树 - service列表
     */
    private JXTree tree;

    /**
     * 下拉框 - 选择选择请求方法
     */
    private ComboBox<RequestMethod> requestMethod;
    /**
     * 输入框 - url地址
     */
    private JBTextField requestUrl;
    /**
     * 按钮 - 发送请求
     */
    private JXButton sendRequest;

    /**
     * 选项卡面板 - 请求信息
     */
    private JBTabbedPane tabbedPane;
    /**
     * 文本域 - 请求头
     */
    private JXEditorPane requestHead;
    /**
     * 文本域 - 请求体
     */
    private JXEditorPane requestBody;
    /**
     * 标签 - 显示返回结果
     */
    private JBLabel responseView;

    /**
     * Create the panel.
     */
    public WindowFrame(@NotNull Project project) {
        this.project = project;

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

        JPanel bodyPanel = new JPanel();
        GridBagConstraints gbcBodyPanel = new GridBagConstraints();
        gbcBodyPanel.weighty = 1.0;
        gbcBodyPanel.fill = GridBagConstraints.BOTH;
        gbcBodyPanel.gridx = 0;
        gbcBodyPanel.gridy = 1;
        add(bodyPanel, gbcBodyPanel);
        bodyPanel.setLayout(new BorderLayout(0, 0));

        JPanel panelInput = new JPanel();
        bodyPanel.add(panelInput, BorderLayout.NORTH);
        panelInput.setLayout(new BorderLayout(0, 0));

        initViewOfRequest(panelInput);
        initViewOfResponse(bodyPanel);

        initEvent();

        firstLoad();
    }

    private void initView(@NotNull JPanel headPanel) {
        scanApi = new JXButton("扫描");
        headPanel.add(scanApi, BorderLayout.NORTH);

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

    private void initViewOfRequest(@NotNull JPanel panelInput) {
        requestMethod = new ComboBox<>(RequestMethod.values());
        panelInput.add(requestMethod, BorderLayout.WEST);

        requestUrl = new JBTextField();
        panelInput.add(requestUrl);
        requestUrl.setColumns(45);

        sendRequest = new JXButton("send");
        panelInput.add(sendRequest, BorderLayout.EAST);
    }

    private void initViewOfResponse(@NotNull JPanel bodyPanel) {
        tabbedPane = new JBTabbedPane(JTabbedPane.TOP);
        bodyPanel.add(tabbedPane, BorderLayout.CENTER);

        JScrollPane scrollPaneHead = new JBScrollPane();
        scrollPaneHead.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tabbedPane.addTab("head", null, scrollPaneHead, null);

        requestHead = new JXEditorPane();
        scrollPaneHead.setViewportView(requestHead);

        JScrollPane scrollPaneBody = new JBScrollPane();
        scrollPaneBody.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tabbedPane.addTab("body", null, scrollPaneBody, null);

        requestBody = new JXEditorPane();
        scrollPaneBody.setViewportView(requestBody);

        JScrollPane scrollPane = new JBScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tabbedPane.addTab("response", null, scrollPane, null);

        responseView = new JBLabel();
        responseView.setVerticalAlignment(SwingConstants.TOP);
        scrollPane.setViewportView(responseView);
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
            setRequest(node);
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

        // 发送请求按钮监听
        sendRequest.addActionListener(e -> {
            // 选择Response页面
            tabbedPane.setSelectedIndex(2);

            RequestMethod method = (RequestMethod) requestMethod.getSelectedItem();
            String url = requestUrl.getText();

            if (url == null || "".equals(url.trim())) {
                responseView.setText("request path must be not empty!");
                return;
            }

            String head = requestHead.getText();
            String body = requestBody.getText();

            String resp = RestUtil.sendRequest(method, url, head, body);
            responseView.setText(resp);
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
            DefaultMutableTreeNode item = new DefaultMutableTreeNode(moduleName);
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

    public void setRequest(Request request) {
        RequestMethod selItem = RequestMethod.GET;
        String reqUrl = "";
        String reqBody = "";

        if (request != null) {
            // 选择Body页面
            tabbedPane.setSelectedIndex(1);

            selItem = request.getMethod() == null ? RequestMethod.GET : request.getMethod();

            GlobalSearchScope scope = request.getPsiMethod().getResolveScope();
            reqUrl = RestUtil.getRequestUrl(
                    RestUtil.scanListenerProtocol(project, scope),
                    RestUtil.scanListenerPort(project, scope),
                    request.getPath()
            );

            reqBody = RestUtil.getRequestParamsTempData(request.getPsiMethod());
        }

        requestMethod.setSelectedItem(selItem);
        requestUrl.setText(reqUrl);
        requestBody.setText(reqBody);
    }
}
