package com.github.restful.tool.view.window.frame;

import com.github.restful.tool.actions.RefreshAction;
import com.github.restful.tool.actions.ScanFilterAction;
import com.github.restful.tool.actions.WithLibraryAction;
import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.beans.Request;
import com.github.restful.tool.service.topic.RefreshServiceTreeTopic;
import com.github.restful.tool.service.topic.RestDetailTopic;
import com.github.restful.tool.service.topic.ServiceTreeTopic;
import com.github.restful.tool.utils.PomUtil;
import com.github.restful.tool.utils.RequestUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhangYuanSheng
 */
public class RightToolWindow extends JSplitPane {

    public static final Map<HttpMethod, Boolean> METHOD_CHOOSE_MAP;
    private static final double WINDOW_WEIGHT = 0.55D;

    static {
        HttpMethod[] values = HttpMethod.values();
        METHOD_CHOOSE_MAP = new ConcurrentHashMap<>(values.length);
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
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.addAll(
                new RefreshAction(this),
                ActionManager.getInstance().getAction("Tool.GotoRequestService"),
                new Separator(),
                new ScanFilterAction(this),
                new WithLibraryAction(this)
        );
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(
                ActionPlaces.TOOLBAR,
                actionGroup,
                true
        );
        actionToolbar.setTargetComponent(this);
        headPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);

        headPanel.add(this.serviceTree, BorderLayout.CENTER);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        this.serviceTree.showPopupMenu();
        this.serviceTree.setChooseRequestCallback(restDetail::chooseRequest);
        this.restDetail.setCallback(this::refreshRequestTree);

        project.getMessageBus().connect().subscribe(ServiceTreeTopic.TOPIC, serviceTree::renderRequestTree);
        project.getMessageBus().connect().subscribe(RefreshServiceTreeTopic.TOPIC, this::refreshRequestTree);
    }

    private void firstLoad() {
        refreshRequestTree();
    }

    @NotNull
    private Map<String, List<Request>> getRequests() {
        Map<String, List<Request>> allRequest = RequestUtil.getAllRequests(project);

        allRequest.forEach((moduleName, requests) -> requests.removeIf(next -> !METHOD_CHOOSE_MAP.get(next.getMethod())));

        return allRequest;
    }

    @NotNull
    public Project getProject() {
        return this.project;
    }

    public void refreshRequestTree() {
        restDetail.reset();

        ServiceTreeTopic serviceTreeTopic = project.getMessageBus().syncPublisher(ServiceTreeTopic.TOPIC);
        DumbService.getInstance(project).runWhenSmart(() -> serviceTreeTopic.action(getRequests()));

        // 清除RestDetail中的Cache缓存
        RestDetailTopic restDetailTopic = project.getMessageBus().syncPublisher(RestDetailTopic.TOPIC);
        restDetailTopic.clearCache(null);

        // 清除扫描的pom文件缓存
        PomUtil.clearCaches();
    }
}
