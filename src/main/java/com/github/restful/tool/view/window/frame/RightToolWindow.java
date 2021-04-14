package com.github.restful.tool.view.window.frame;

import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.beans.Request;
import com.github.restful.tool.service.topic.RefreshServiceTreeTopic;
import com.github.restful.tool.service.topic.RestDetailTopic;
import com.github.restful.tool.service.topic.ServiceTreeTopic;
import com.github.restful.tool.utils.PomUtil;
import com.github.restful.tool.utils.RequestUtil;
import com.github.restful.tool.view.window.RestfulToolWindowFactory;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.JBSplitter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhangYuanSheng
 */
public class RightToolWindow extends JPanel {

    public static final Map<HttpMethod, Boolean> METHOD_CHOOSE_MAP;
    private static final float DEFAULT_PROPORTION = 0.5F;

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
        super(new BorderLayout());
        this.project = project;

        this.serviceTree = new ServiceTree(project);
        this.restDetail = new RestDetail(project);

        AnAction action = ActionManager.getInstance().getAction(RestfulToolWindowFactory.TOOL_WINDOW_ID + ".Toolbar");
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(
                ActionPlaces.TOOLBAR,
                action instanceof ActionGroup ? ((ActionGroup) action) : new DefaultActionGroup(),
                true
        );
        actionToolbar.setTargetComponent(this);
        this.add(actionToolbar.getComponent(), BorderLayout.NORTH);

        JBSplitter splitter = new JBSplitter(true, RightToolWindow.class.getName(), DEFAULT_PROPORTION);
        splitter.setFirstComponent(this.serviceTree);
        splitter.setSecondComponent(this.restDetail);
        this.add(splitter, BorderLayout.CENTER);

        initEvent();

        DumbService.getInstance(project).smartInvokeLater(this::firstLoad);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
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

    public void navigationToView(@NotNull PsiMethod psiMethod) {
        RestfulToolWindowFactory.showWindow(project, () -> serviceTree.navigationToTree(psiMethod));
    }
}
