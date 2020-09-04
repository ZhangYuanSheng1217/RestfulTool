/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RestDetail
  Author:   ZhangYuanSheng
  Date:     2020/5/21 23:54
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window.frame;

import cn.hutool.http.*;
import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.beans.Request;
import com.github.restful.tool.beans.settings.Settings;
import com.github.restful.tool.service.topic.RestDetailTopic;
import com.github.restful.tool.utils.Bundle;
import com.github.restful.tool.utils.JsonUtil;
import com.github.restful.tool.utils.RestUtil;
import com.github.restful.tool.utils.SystemUtil;
import com.github.restful.tool.utils.convert.BaseConvert;
import com.github.restful.tool.utils.convert.JsonConvert;
import com.github.restful.tool.view.components.editor.JsonEditor;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.messages.MessageBusConnection;
import org.intellij.lang.annotations.Language;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestDetail extends JPanel {

    private static final int REQUEST_TIMEOUT = 1000 * 10;

    private static final String IDENTITY_HEAD = "HEAD";
    private static final String IDENTITY_BODY = "BODY";

    private final Project project;
    private final ThreadPoolExecutor poolExecutor;
    private final BaseConvert<?> convert;
    private final Map<Request, String> headCache;
    private final Map<Request, String> bodyCache;
    /**
     * 下拉框 - 选择选择请求方法
     */
    private JComboBox<HttpMethod> requestMethod;
    /**
     * 输入框 - url地址
     */
    private JTextField requestUrl;
    /**
     * 按钮 - 发送请求
     */
    private JButton sendRequest;
    /**
     * 选项卡面板 - 请求信息
     */
    private JBTabs tabs;
    private JsonEditor requestHead;
    /**
     * 文本域 - 请求体
     */
    private TabInfo bodyTab;
    private JsonEditor requestBody;
    /**
     * 标签 - 显示返回结果
     */
    private TabInfo responseTab;
    private JsonEditor responseView;

    private DetailHandle callback;
    /**
     * 选中的Request
     */
    private Request chooseRequest;

    public RestDetail(@NotNull Project project) {
        this.project = project;
        this.poolExecutor = new ThreadPoolExecutor(
                1,
                1,
                1000,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(8),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        // this.convert = new DefaultConvert();
        this.convert = new JsonConvert();

        this.headCache = new HashMap<>();
        this.bodyCache = new HashMap<>();

        initView();

        initEvent();
    }

    private void initView() {
        setLayout(new BorderLayout(0, 0));

        JPanel panelInput = new JPanel();
        add(panelInput, BorderLayout.NORTH);
        panelInput.setLayout(new BorderLayout(0, 0));

        requestMethod = new ComboBox<>(HttpMethod.getValues());
        panelInput.add(requestMethod, BorderLayout.WEST);

        requestUrl = new JBTextField();
        panelInput.add(requestUrl);
        requestUrl.setColumns(45);

        sendRequest = new JXButton(Bundle.getString("http.tool.button.send"));
        panelInput.add(sendRequest, BorderLayout.EAST);

        tabs = new JBTabsImpl(project);

        requestHead = new JsonEditor(project, JsonEditor.JSON_FILE_TYPE);
        requestHead.setName(IDENTITY_HEAD);

        TabInfo headTab = new TabInfo(requestHead);
        headTab.setText(Bundle.getString("http.tool.tab.head"));
        tabs.addTab(headTab);

        requestBody = new JsonEditor(project, JsonEditor.JSON_FILE_TYPE);
        requestBody.setName(IDENTITY_BODY);
        bodyTab = new TabInfo(requestBody);
        bodyTab.setText(Bundle.getString("http.tool.tab.body"));
        tabs.addTab(bodyTab);

        responseView = new JsonEditor(project);
        responseTab = new TabInfo(responseView);
        responseTab.setText(Bundle.getString("http.tool.tab.response"));
        tabs.addTab(responseTab);

        add(tabs.getComponent(), BorderLayout.CENTER);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        // 发送请求按钮监听
        sendRequest.addActionListener(event -> {
            String url = requestUrl.getText();
            if (url == null || "".equals(url.trim())) {
                requestUrl.requestFocus();
                return;
            }

            // 选择Response页面
            tabs.select(responseTab, true);
            sendRequest(url);
        });

        MessageBusConnection messageBusConnection = project.getMessageBus().connect();
        messageBusConnection.subscribe(RestDetailTopic.TOPIC, request -> {
            if (request != null) {
                headCache.remove(request);
                bodyCache.remove(request);
            } else {
                headCache.clear();
                bodyCache.clear();
            }
        });

        DocumentListener documentListenerForCache = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                JsonEditor editor = getCurrentTabbedOfRequest();
                if (editor != null && chooseRequest != null) {
                    String name = editor.getName();
                    String text = editor.getText();
                    setCache(name, chooseRequest, text);
                }
            }
        };
        // fixBug: 无法正确绑定监听事件，导致无法缓存单个request的请求头或请求参数的数据
        FocusAdapter focusAdapter = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                JsonEditor editor = getCurrentTabbedOfRequest();
                if (editor != null) {
                    editor.addDocumentListener(documentListenerForCache);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                JsonEditor editor = getCurrentTabbedOfRequest();
                if (editor != null) {
                    editor.removeDocumentListener(documentListenerForCache);
                }
            }
        };
        requestHead.addFocusListener(focusAdapter);
        requestBody.addFocusListener(focusAdapter);
    }

    private void sendRequest(String url) {
        HttpMethod method = (HttpMethod) requestMethod.getSelectedItem();
        if (method == null) {
            method = HttpMethod.GET;
        }
        HttpRequest httpRequest = getHttpRequest(method, url, requestHead.getText(), requestBody.getText());

        Runnable command = () -> {
            Application application = ApplicationManager.getApplication();
            application.invokeLater(() -> responseView.setPlaceholder(Bundle.getString("http.tool.running.http")));
            try {
                HttpResponse execute = httpRequest.execute();
                // 最大重定向的次数
                final int redirectMaxCount = Settings.HttpToolOptionForm.REDIRECT_MAX_COUNT.getData();
                int redirectCount = 0;
                while (redirectCount++ < redirectMaxCount && execute.getStatus() == HttpStatus.HTTP_MOVED_TEMP) {
                    String redirect = execute.header(Header.LOCATION);
                    httpRequest.setUrl(redirect);
                    execute = httpRequest.execute();
                }

                @Language("RegExp") final String regJsonContext = "application/json";
                @Language("RegExp") final String regHtml = "text/html";
                @Language("RegExp") final String regXml = "text/xml";

                FileType fileType = JsonEditor.TEXT_FILE_TYPE;
                // Content-Type
                final String contentType = execute.header(Header.CONTENT_TYPE);
                if (contentType != null) {
                    if (compileRegExp(regJsonContext).matcher(contentType).find()) {
                        fileType = JsonEditor.JSON_FILE_TYPE;
                    } else if (compileRegExp(regHtml).matcher(contentType).find()) {
                        fileType = JsonEditor.HTML_FILE_TYPE;
                    } else if (compileRegExp(regXml).matcher(contentType).find()) {
                        fileType = JsonEditor.XML_FILE_TYPE;
                    }
                }
                final FileType finalFileType = fileType;
                final String responseBody = execute.body();
                application.invokeLater(() -> responseView.setText(responseBody, finalFileType));
            } catch (Exception e) {
                final String response = String.format("%s", e);
                application.invokeLater(() -> responseView.setText(response, JsonEditor.TEXT_FILE_TYPE));
            }
            application.invokeLater(() -> responseView.setPlaceholder(null));
        };
        responseView.setText(null);
        poolExecutor.execute(command);
    }

    @Nullable
    private JsonEditor getCurrentTabbedOfRequest() {
        TabInfo tabInfo = tabs.getSelectedInfo();
        if (tabInfo == null) {
            return null;
        }
        Component component = tabInfo.getComponent();
        if (component instanceof JsonEditor) {
            return (JsonEditor) component;
        }
        return null;
    }

    public void chooseRequest(@Nullable Request request) {
        this.chooseRequest = request;

        HttpMethod selItem = HttpMethod.GET;
        String reqUrl = null;
        String reqHead = null;
        String reqBody = null;

        try {
            if (request != null) {
                GlobalSearchScope scope = request.getPsiMethod().getResolveScope();
                reqUrl = SystemUtil.buildUrl(
                        RestUtil.scanListenerProtocol(project, scope),
                        RestUtil.scanListenerPort(project, scope),
                        RestUtil.scanContextPath(project, scope),
                        request.getPath()
                );

                // 选择Body页面
                tabs.select(bodyTab, false);

                selItem = request.getMethod() == null || request.getMethod() == HttpMethod.REQUEST ?
                        HttpMethod.GET : request.getMethod();

                if (headCache.containsKey(request)) {
                    reqHead = getCache(IDENTITY_HEAD, request);
                } else {
                    reqHead = String.format(
                            "{\n  \"Content-Type\": \"%s\"\n}",
                            Settings.HttpToolOptionForm.CONTENT_TYPE.getData().getValue()
                    );
                    setCache(IDENTITY_HEAD, request, reqHead);
                }

                if (bodyCache.containsKey(request)) {
                    reqBody = getCache(IDENTITY_BODY, request);
                } else {
                    convert.setPsiMethod(request.getPsiMethod());
                    reqBody = convert.formatString();
                    setCache(IDENTITY_BODY, request, reqBody);
                }
            }
        } catch (PsiInvalidElementAccessException e) {
            /*
            @Throws Code: request.getPsiMethod().getResolveScope()
            @Throws Message: 无效访问，通常代表指向方法已被删除
             */
            if (callback != null) {
                callback.handle();
            }
        }

        requestMethod.setSelectedItem(selItem);
        requestUrl.setText(reqUrl);
        requestHead.setText(reqHead);
        requestBody.setText(reqBody);
        responseView.setText(null);
    }

    public void setCallback(DetailHandle callback) {
        this.callback = callback;
    }

    public void reset() {
        this.chooseRequest(null);
    }

    private HttpRequest getHttpRequest(@NotNull HttpMethod method, @NotNull String url, String head, String body) {
        HttpRequest request = HttpUtil.createRequest(Method.valueOf(method.name()), url);
        if (head != null && !"".equals(head.trim())) {
            convert.formatMap(head).forEach((s, o) -> request.header(s, (String) o));
        }
        if (body != null && !"".equals(body.trim())) {
            Map<String, ?> formatMap = convert.formatMap(body);
            if (!convert.isRaw()) {
                formatMap.forEach(request::form);
            } else {
                String requestBody;
                if (convert.isBasicDataTypes()) {
                    requestBody = (String) formatMap.get(convert.getBasicDataParamName());
                } else {
                    requestBody = JsonUtil.formatJson(formatMap);
                }
                request.body(requestBody, "application/json");
            }
        }
        return request.timeout(REQUEST_TIMEOUT);
    }

    @NotNull
    private String getCache(@NotNull String name, @NotNull Request request) {
        boolean enable = Settings.HttpToolOptionForm.ENABLE_CACHE_OF_REST_DETAIL.getData();
        if (enable) {
            switch (name) {
                case IDENTITY_HEAD:
                    return headCache.getOrDefault(request, "");
                case IDENTITY_BODY:
                    return bodyCache.getOrDefault(request, "");
                default:
                    break;
            }
        }
        return "";
    }

    private void setCache(@NotNull String name, @NotNull Request request, @NotNull String cache) {
        boolean enable = Settings.HttpToolOptionForm.ENABLE_CACHE_OF_REST_DETAIL.getData();
        if (enable) {
            switch (name) {
                case IDENTITY_HEAD:
                    if (cache.equals(headCache.get(request))) {
                        return;
                    }
                    headCache.put(request, cache);
                    break;
                case IDENTITY_BODY:
                    if (cache.equals(bodyCache.get(request))) {
                        return;
                    }
                    bodyCache.put(request, cache);
                    break;
                default:
                    break;
            }
        }
    }

    @NotNull
    private Pattern compileRegExp(@NotNull final String reg) {
        return Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
    }

    public interface DetailHandle {

        /**
         * 处理逻辑
         */
        void handle();
    }
}
