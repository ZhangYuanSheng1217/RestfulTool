package com.github.restful.tool.view.window.frame;

import com.github.restful.tool.actions.RefreshAction;
import com.github.restful.tool.actions.WithLibraryAction;
import com.github.restful.tool.actions.filters.HttpMethodFilterAction;
import com.github.restful.tool.actions.filters.ModuleFilterAction;
import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.beans.ModuleTree;
import com.github.restful.tool.beans.settings.Settings;
import com.github.restful.tool.service.Notify;
import com.github.restful.tool.service.topic.RefreshServiceTreeTopic;
import com.github.restful.tool.service.topic.RestDetailTopic;
import com.github.restful.tool.service.topic.ServiceTreeTopic;
import com.github.restful.tool.utils.ApiServices;
import com.github.restful.tool.utils.Async;
import com.github.restful.tool.view.components.tree.BaseNode;
import com.github.restful.tool.view.window.WindowFactory;
import com.github.restful.tool.view.components.tree.node.ModuleNode;
import com.github.restful.tool.view.components.tree.node.RootNode;
import com.github.restful.tool.view.components.tree.node.ServiceNode;
import com.intellij.ide.CommonActionsManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.JBSplitter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author ZhangYuanSheng
 */
public class Window extends SimpleToolWindowPanel implements Disposable {

    /**
     * 限制同时运行的任务数量。提交的任务数量没有限制
     */
    private static final ExecutorService EXECUTOR_TASK_BOUNDED = new ThreadPoolExecutor(
            1,
            1,
            5L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()
    );

    private static final Map<HttpMethod, Boolean> METHOD_CHOOSE_MAP;
    private static final Map<Module, Boolean> MODULES_CHOOSE_MAP;

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
    private final transient Project project;
    private final ApiServiceListPanel apiServiceListPanel;
    private final HttpTestPanel httpTestPanel;

    /**
     * Create the panel.
     */
    public Window(@NotNull Project project) {
        super(false, false);
        this.project = project;

        this.apiServiceListPanel = new ApiServiceListPanel(project);
        this.httpTestPanel = new HttpTestPanel(project);

        ActionToolbar toolbar = initToolbar();
        setToolbar(toolbar.getComponent());

        JBSplitter content = initContent();
        setContent(content);

        initEvent();
        DumbService.getInstance(project).smartInvokeLater(
                () -> Async.runRead(project, () -> {
                    resetModules();
                    return this.getRequests();
                }, this::renderApiServiceTree)
        );

        Disposer.register(project, this);
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

    public void refresh() {
        Async.runRead(project, this::getRequests, data -> {
            httpTestPanel.reset();

            renderApiServiceTree(data);

            // 清除RestDetail中的Cache缓存
            RestDetailTopic restDetailTopic = project.getMessageBus().syncPublisher(RestDetailTopic.TOPIC);
            restDetailTopic.clearCache(null);

            // 装载Module列表
            resetModules();
        });
    }

    public void refreshOnFilter() {
        Async.runRead(project, this::getRequests, this::renderApiServiceTree);
    }

    public void navigationToView(@NotNull PsiMethod psiMethod) {
        WindowFactory.showWindow(project, () -> apiServiceListPanel.navigationToTree(psiMethod));
    }

    @NotNull
    private ActionToolbar initToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new RefreshAction());
        group.add(ActionManager.getInstance().getAction("Tool.GotoRequestService"));

        group.addSeparator();

        group.add(new HttpMethodFilterAction());
        group.add(new ModuleFilterAction());

        group.addSeparator();

        group.add(new WithLibraryAction());
        group.add(CommonActionsManager.getInstance().createExpandAllAction(apiServiceListPanel, this));
        group.add(CommonActionsManager.getInstance().createCollapseAllAction(apiServiceListPanel, this));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(
                ActionPlaces.TOOLBAR,
                group,
                true
        );
        toolbar.setTargetComponent(this);

        return toolbar;
    }

    @NotNull
    private JBSplitter initContent() {
        JBSplitter contentView = new JBSplitter(true, Window.class.getName(), 0.5F);
        contentView.setFirstComponent(this.apiServiceListPanel);
        contentView.setSecondComponent(this.httpTestPanel);
        return contentView;
    }

    private void initEvent() {
        this.apiServiceListPanel.setChooseCallback(apiService -> {
            try {
                httpTestPanel.chooseRequest(apiService);
            } catch (NullPointerException e) {
                // IdeaEventQueue 调用异常情况(极少数情况)
                refresh();
                Notify.getInstance(project).warning("Call exception for `com.intellij.ide.IdeEventQueue`");
            }
        });
        this.httpTestPanel.setCallback(this::refresh);

        project.getMessageBus().connect().subscribe(ServiceTreeTopic.TOPIC, this::renderApiServiceTree);
        project.getMessageBus().connect().subscribe(RefreshServiceTreeTopic.TOPIC, this::refresh);
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
            allRequest = ApiServices.getApis(project, getFilterModules(true), false);
        }

        allRequest.forEach((moduleName, requests) -> requests.removeIf(next -> !METHOD_CHOOSE_MAP.get(next.getMethod())));

        return allRequest;
    }

    private void renderApiServiceTree(Map<String, List<ApiService>> apiServices) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Reload restful apis", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);

                Map<PsiMethod, ServiceNode> serviceNodes = new HashMap<>();
                Callable<RootNode> producer = () -> {
                    if (indicator.isCanceled()) {
                        return null;
                    }
                    RootNode root = new RootNode("Find empty");
                    indicator.setText("Initialize");

                    apiServices.entrySet().stream()
                            .map(entry -> {
                                String itemName = entry.getKey();
                                List<ApiService> requests = entry.getValue();
                                if (requests == null || requests.isEmpty()) {
                                    return null;
                                }
                                ModuleNode moduleNode = new ModuleNode(new ModuleTree(itemName));
                                Boolean showClass = Settings.SystemOptionForm.SHOW_CLASS_SERVICE_TREE.getData();
                                if (showClass != null && showClass) {
                                    Map<PsiClass, List<ApiService>> listMap = requests.stream().collect(
                                            Collectors.toMap(
                                                    // key: PsiClass
                                                    apiService -> {
                                                        NavigatablePsiElement psiElement = apiService.getPsiElement();
                                                        PsiElement parent = psiElement.getParent();
                                                        if (parent instanceof PsiClass) {
                                                            return ((PsiClass) parent);
                                                        }
                                                        return null;
                                                    },
                                                    // value: List<ApiService>
                                                    apiService -> new ArrayList<>(Collections.singletonList(apiService)),
                                                    // key冲突时的操作
                                                    (list1, list2) -> {
                                                        list1.addAll(list2);
                                                        return list1;
                                                    }
                                            )
                                    );
                                    List<BaseNode<?>> children = ModuleNode.Util.getChildren(serviceNodes, listMap, showClass);
                                    children.forEach(moduleNode::add);
                                } else {
                                    List<BaseNode<?>> children = ModuleNode.Util.getChildren(serviceNodes, requests);
                                    children.forEach(moduleNode::add);
                                }
                                return moduleNode;
                            })
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparing(moduleNode -> moduleNode.getSource().getModuleName()))
                            .forEach(root::add);
                    indicator.setText("Waiting to re-render");
                    return root;
                };
                Consumer<RootNode> consumer = root -> {
                    if (root == null) {
                        return;
                    }

                    if (!serviceNodes.isEmpty()) {
                        root.setSource("Find " + serviceNodes.size() + " apis");
                    }
                    apiServiceListPanel.renderAll(
                            root,
                            serviceNodes,
                            Settings.SystemOptionForm.EXPAND_OF_SERVICE_TREE.getData()
                    );
                };
                ReadAction
                        .nonBlocking(producer)
                        .finishOnUiThread(ModalityState.defaultModalityState(), consumer)
                        .submit(EXECUTOR_TASK_BOUNDED);
            }
        });
    }

    @Override
    public void dispose() {
        EXECUTOR_TASK_BOUNDED.shutdown();
    }
}
