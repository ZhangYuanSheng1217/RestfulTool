package com.github.restful.tool.view.window.frame;

import com.github.restful.tool.beans.PropertiesKey;
import com.github.restful.tool.service.topic.RefreshServiceTreeTopic;
import com.github.restful.tool.service.topic.RestDetailTopic;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.beans.Request;
import com.github.restful.tool.service.topic.ServiceTreeTopic;
import com.github.restful.tool.utils.RequestUtil;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final ServiceTree serviceTree;
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
     * Create the panel.
     */
    public RightToolWindow(@NotNull Project project) {
        super(VERTICAL_SPLIT);

        this.project = project;
        this.serviceTree = new ServiceTree(project);
        this.restDetail = new RestDetail(project);

        setContinuousLayout(true);
        setResizeWeight(WINDOW_WEIGHT);
        setDividerSize(2);
        setBorder(JBUI.Borders.empty());

        JPanel headPanel = new JPanel(new BorderLayout());
        initView(headPanel);
        setTopComponent(headPanel);

        setBottomComponent(this.restDetail);

        initEvent();

        DumbService.getInstance(project).smartInvokeLater(this::firstLoad);
    }

    private void initView(@NotNull JPanel headPanel) {
        JPanel toolBar = new JPanel();
        toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));
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

        headPanel.add(this.serviceTree, BorderLayout.CENTER);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        this.serviceTree.showPopupMenu();
        this.serviceTree.setChooseRequestCallback(restDetail::setRequest);
        this.restDetail.setCallback(this::renderRequestTree);

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

        project.getMessageBus().connect().subscribe(ServiceTreeTopic.TOPIC, serviceTree::renderRequestTree);
        project.getMessageBus().connect().subscribe(RefreshServiceTreeTopic.TOPIC, this::renderRequestTree);
    }

    private void firstLoad() {
        renderRequestTree();
    }

    @NotNull
    private Map<String, List<Request>> getRequests() {
        Map<String, List<Request>> allRequest = RequestUtil.getAllRequests(project);

        allRequest.forEach((moduleName, requests) -> requests.removeIf(next -> !METHOD_CHOOSE_MAP.get(next.getMethod())));

        return allRequest;
    }

    public void renderRequestTree() {
        ServiceTreeTopic restTopic = project.getMessageBus().syncPublisher(ServiceTreeTopic.TOPIC);
        DumbService.getInstance(project).runWhenSmart(() -> restTopic.action(getRequests()));

        // 清除RestDetail中的Cache缓存
        RestDetailTopic restDetailTopic = project.getMessageBus().syncPublisher(RestDetailTopic.TOPIC);
        restDetailTopic.clearCache(null);
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
