package com.github.restful.tool.view.window.frame;

import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.service.topic.RefreshServiceTreeTopic;
import com.github.restful.tool.service.topic.RestDetailTopic;
import com.github.restful.tool.service.topic.ServiceTreeTopic;
import com.github.restful.tool.utils.ApiServiceUtil;
import com.github.restful.tool.utils.Async;
import com.github.restful.tool.utils.PomUtil;
import com.github.restful.tool.view.window.WindowFactory;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.JBSplitter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhangYuanSheng
 */
public class Window extends JPanel {

    private static final Map<HttpMethod, Boolean> METHOD_CHOOSE_MAP;
    private static final Map<Module, Boolean> MODULES_CHOOSE_MAP;
    private static final float DEFAULT_PROPORTION = 0.5F;

    static {
        MODULES_CHOOSE_MAP = new HashMap<>();

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
    private final ApiServiceListPanel apiServiceListPanel;
    private final HttpTestPanel httpTestPanel;

    /**
     * Create the panel.
     */
    public Window(@NotNull Project project) {
        super(new BorderLayout());
        this.project = project;

        this.apiServiceListPanel = new ApiServiceListPanel(project);
        this.httpTestPanel = new HttpTestPanel(project);

        AnAction action = ActionManager.getInstance().getAction(WindowFactory.TOOL_WINDOW_ID + ".Toolbar");
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(
                ActionPlaces.TOOLBAR,
                action instanceof ActionGroup ? ((ActionGroup) action) : new DefaultActionGroup(),
                true
        );
        actionToolbar.setTargetComponent(this);
        this.add(actionToolbar.getComponent(), BorderLayout.NORTH);

        JBSplitter splitter = new JBSplitter(true, Window.class.getName(), DEFAULT_PROPORTION);
        splitter.setFirstComponent(this.apiServiceListPanel);
        splitter.setSecondComponent(this.httpTestPanel);
        this.add(splitter, BorderLayout.CENTER);

        DumbService.getInstance(project).smartInvokeLater(this::firstLoad);
    }

    public static void setMethodFilterStatus(@NotNull HttpMethod method, @NotNull Boolean selected) {
        METHOD_CHOOSE_MAP.put(method, selected);
    }

    public static void setModuleFilterStatus(@NotNull Module module, @NotNull Boolean selected) {
        MODULES_CHOOSE_MAP.put(module, selected);
    }

    @NotNull
    public static HttpMethod @NotNull [] getFilterMethods(boolean selected) {
        return METHOD_CHOOSE_MAP.entrySet()
                .stream()
                .filter(item -> item != null && item.getKey() != null && item.getValue() != null && (!selected || item.getValue()))
                .map(Map.Entry::getKey)
                .toArray(HttpMethod[]::new);
    }

    @NotNull
    public static Module @NotNull [] getFilterModules(boolean selected) {
        return MODULES_CHOOSE_MAP.entrySet()
                .stream()
                .filter(item -> item != null && item.getKey() != null && item.getValue() != null && (!selected || item.getValue()))
                .map(Map.Entry::getKey)
                .toArray(Module[]::new);
    }

    public static Boolean hasSelectedFilterMethod(@NotNull HttpMethod method) {
        return METHOD_CHOOSE_MAP.getOrDefault(method, null);
    }

    public static Boolean hasSelectedFilterModule(@NotNull Module module) {
        return MODULES_CHOOSE_MAP.getOrDefault(module, null);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        this.apiServiceListPanel.setChooseRequestCallback(httpTestPanel::chooseRequest);
        this.httpTestPanel.setCallback(this::refreshRequestTree);

        project.getMessageBus().connect().subscribe(ServiceTreeTopic.TOPIC, apiServiceListPanel::renderRequestTree);
        project.getMessageBus().connect().subscribe(RefreshServiceTreeTopic.TOPIC, this::refreshRequestTree);
    }

    private void firstLoad() {
        initEvent();

        Async.runRead(project, () -> {
            resetModules();
            return this.getRequests();
        }, apiServiceListPanel::renderRequestTree);
    }

    private void resetModules() {
        MODULES_CHOOSE_MAP.clear();
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            MODULES_CHOOSE_MAP.put(module, true);
        }
    }

    @NotNull
    private Map<String, List<ApiService>> getRequests() {
        Map<String, List<ApiService>> allRequest;
        if (MODULES_CHOOSE_MAP.isEmpty()) {
            allRequest = Collections.emptyMap();
        } else {
            allRequest = ApiServiceUtil.getApis(project, getFilterModules(true), false);
        }

        allRequest.forEach((moduleName, requests) -> requests.removeIf(next -> !METHOD_CHOOSE_MAP.get(next.getMethod())));

        return allRequest;
    }

    @NotNull
    public Project getProject() {
        return this.project;
    }

    public void refreshRequestTree() {
        Async.runRead(project, this::getRequests, data -> {
            httpTestPanel.reset();

            apiServiceListPanel.renderRequestTree(data);

            // 清除RestDetail中的Cache缓存
            RestDetailTopic restDetailTopic = project.getMessageBus().syncPublisher(RestDetailTopic.TOPIC);
            restDetailTopic.clearCache(null);

            // 清除扫描的pom文件缓存
            PomUtil.clearCaches();

            // 装载Module列表
            resetModules();
        });
    }

    public void refreshRequestTreeOnFilter() {
        Async.runRead(project, this::getRequests, apiServiceListPanel::renderRequestTree);
    }

    public void navigationToView(@NotNull PsiMethod psiMethod) {
        WindowFactory.showWindow(project, () -> apiServiceListPanel.navigationToTree(psiMethod));
    }
}
