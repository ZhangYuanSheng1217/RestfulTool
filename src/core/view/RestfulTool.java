/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RestfulTool
  Author:   ZhangYuanSheng
  Date:     2020/4/29 23:02
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package core.view;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import core.beans.Request;
import core.beans.RequestMethod;
import core.utils.RestUtil;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestfulTool {

    private JPanel content;

    private JComboBox<RequestMethod> requestMethod;
    private JBTextField requestUrl;
    private JXButton sendRequest;
    private JTree tree;
    private JXButton scanApi;

    private JBTabbedPane tabbedPane;
    private JBTextArea requestHead;
    private JBTextArea requestBody;
    private JLabel responseView;

    public RestfulTool(@NotNull Project project) {
        System.out.println("RestfulTool:::RestfulTool");

        initView();

        initEvent(project);

        try {
            renderRequestTree(project);
        } catch (Exception e) {
            DumbService.getInstance(project).showDumbModeNotification(e.getMessage());
        }
    }

    /**
     * 渲染Restful请求列表
     *
     * @param project project
     */
    private void renderRequestTree(@NotNull Project project) {
        AtomicInteger controllerCount = new AtomicInteger();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(controllerCount.get());

        Map<String, List<Request>> allRequest = RestUtil.getAllRequest(project);
        allRequest.forEach((moduleName, requests) -> {
            DefaultMutableTreeNode item = new DefaultMutableTreeNode(moduleName);
            requests.forEach(request -> item.add(new DefaultMutableTreeNode(request)));
            root.add(item);
        });

        root.setUserObject(controllerCount.get());
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.setRoot(root);
        expandAll(tree, new TreePath(tree.getModel().getRoot()), true);
    }

    private void initView() {
        requestMethod.addItem(RequestMethod.GET);
        requestMethod.addItem(RequestMethod.POST);
        requestMethod.addItem(RequestMethod.DELETE);
        requestMethod.addItem(RequestMethod.PATCH);
        requestMethod.addItem(RequestMethod.PUT);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Service");
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.setRoot(root);

        tree.setCellRenderer(new RestfulTreeCellRenderer());
    }

    /**
     * 初始化事件
     */
    private void initEvent(@NotNull Project project) {
        // 控制器扫描监听
        scanApi.addActionListener(e -> renderRequestTree(project));

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

        // RequestTree子项点击监听
        tree.addTreeSelectionListener(e -> {
            Request node = getTreeNodeRequest(tree);
            if (node == null) {
                return;
            }
            // 选择Body页面
            tabbedPane.setSelectedIndex(1);

            requestMethod.setSelectedItem(node.getMethod() == null ? RequestMethod.GET : node.getMethod());

            GlobalSearchScope scope = node.getPsiMethod().getResolveScope();
            requestUrl.setText(getRequestUrl(
                    RestUtil.scanListenerProtocol(project, scope),
                    RestUtil.scanListenerPort(project, scope),
                    node.getPath()
            ));

            requestBody.setText(RestUtil.getRequestParamsTempData(node.getPsiMethod()));
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

    /**
     * 获取url
     *
     * @param protocol 协议
     * @param port     端口
     * @param path     路径
     * @return url
     */
    @NotNull
    private String getRequestUrl(@NotNull String protocol, @Nullable Integer port, String path) {
        StringBuilder url = new StringBuilder(protocol + "://");
        url.append("localhost");
        if (port != null) {
            url.append(":").append(port);
        }
        if (!path.startsWith("/")) {
            url.append("/");
        }
        url.append(path);
        return url.toString();
    }

    public JPanel getContent() {
        return content;
    }
}
