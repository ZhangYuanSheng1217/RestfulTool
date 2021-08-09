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
import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.beans.settings.Settings;
import com.github.restful.tool.service.Notify;
import com.github.restful.tool.service.topic.RestDetailTopic;
import com.github.restful.tool.utils.Async;
import com.github.restful.tool.utils.Bundle;
import com.github.restful.tool.utils.convert.ParamsConvert;
import com.github.restful.tool.view.components.editor.CustomEditor;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.impl.FileTypeRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import org.intellij.lang.annotations.Language;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class HttpTestPanel extends JPanel implements Serializable {

    public static final FileType DEFAULT_FILE_TYPE = CustomEditor.TEXT_FILE_TYPE;
    private static final int REQUEST_TIMEOUT = 1000 * 10;
    private static final String IDENTITY_HEAD = "HEAD";
    private static final String IDENTITY_BODY = "BODY";
    private final transient Project project;
    private final ThreadPoolExecutor poolExecutor;
    private final ParamsConvert convert;

    private final Map<ApiService, String> bodyCache;
    private final Map<ApiService, FileType> bodyTextTypeCache;
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
    /**
     * 文本域 - 请求头
     */
    private TabInfo headTab;
    private CustomEditor requestHead;
    /**
     * 文本域 - 请求体
     */
    private TabInfo bodyTab;
    private CustomEditor requestBody;
    private ComboBox<FileType> requestBodyFileType;
    /**
     * 标签 - 显示返回结果
     */
    private TabInfo responseTab;
    private CustomEditor responseView;

    private DetailHandle callback;
    /**
     * 选中的Request
     */
    private ApiService chooseApiService;

    public HttpTestPanel(@NotNull Project project) {
        this.project = project;
        this.poolExecutor = new ThreadPoolExecutor(
                1,
                1,
                1000,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(8),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        this.convert = new ParamsConvert();

        this.bodyCache = new HashMap<>();
        this.bodyTextTypeCache = new HashMap<>();

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

        requestHead = new CustomEditor(project, DEFAULT_FILE_TYPE);
        requestHead.setName(IDENTITY_HEAD);
        headTab = new TabInfo(requestHead);
        headTab.setText(Bundle.getString("http.tool.tab.head"));
        tabs.addTab(headTab);

        requestBody = new CustomEditor(project, DEFAULT_FILE_TYPE);
        requestBody.setName(IDENTITY_BODY);
        bodyTab = new TabInfo(requestBody);
        bodyTab.setText(Bundle.getString("http.tool.tab.body"));
        tabs.addTab(bodyTab);
        // 设置JsonEditor为JPanel的下一个焦点
        putClientProperty("nextFocus", requestBody);

        responseView = new CustomEditor(project);
        responseTab = new TabInfo(responseView);
        responseTab.setText(Bundle.getString("http.tool.tab.response"));
        tabs.addTab(responseTab);

        add(tabs.getComponent(), BorderLayout.CENTER);

        JPanel bodyFileTypePanel = new JPanel(new BorderLayout());
        bodyFileTypePanel.add(new JBLabel(Bundle.getString("other.restDetail.chooseBodyFileType")), BorderLayout.WEST);
        requestBodyFileType = new ComboBox<>(new FileType[]{
                CustomEditor.TEXT_FILE_TYPE,
                CustomEditor.JSON_FILE_TYPE,
                CustomEditor.HTML_FILE_TYPE,
                CustomEditor.XML_FILE_TYPE
        });
        requestBodyFileType.setFocusable(false);
        bodyFileTypePanel.add(requestBodyFileType, BorderLayout.CENTER);
        bodyFileTypePanel.setBorder(JBUI.Borders.emptyLeft(3));
        // add(bodyFileTypePanel, BorderLayout.SOUTH)
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

        requestBodyFileType.setSelectedItem(getCacheType());
        requestBodyFileType.setRenderer(new FileTypeRenderer());
        requestBodyFileType.addItemListener(e -> {
            Object selectedObject = e.getItemSelectable().getSelectedObjects()[0];
            if (selectedObject instanceof FileType) {
                FileType fileType = (FileType) selectedObject;
                requestBody.setFileType(fileType);
                setCacheType(fileType);
            }
        });

        MessageBusConnection messageBusConnection = project.getMessageBus().connect();
        messageBusConnection.subscribe(RestDetailTopic.TOPIC, request -> {
            if (request != null) {
                bodyCache.remove(request);
                bodyTextTypeCache.remove(request);
            } else {
                bodyCache.clear();
                bodyTextTypeCache.clear();
            }
        });

        DocumentListener documentListenerForCache = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                CustomEditor editor = getCurrentTabbedOfRequest();
                if (editor != null && chooseApiService != null) {
                    String name = editor.getName();
                    String text = editor.getText();
                    setCache(name, chooseApiService, text);
                }
            }
        };
        // fixBug: 无法正确绑定监听事件，导致无法缓存单个request的请求头或请求参数的数据
        FocusAdapter focusAdapter = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                CustomEditor editor = getCurrentTabbedOfRequest();
                if (editor != null) {
                    editor.addDocumentListener(documentListenerForCache);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                CustomEditor editor = getCurrentTabbedOfRequest();
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
        String head = requestHead.getText();
        if ("".equals(head.trim())) {
            head = "{}";
        }
        Map<String, Object> headers = convert.formatMap(head);
        if (headers == null) {
            // 选择Header页面
            tabs.select(headTab, true);
            Notify.getInstance(project).error("Incorrect request header format!");
            return;
        }
        HttpRequest httpRequest = generateHttpRequest(method, url, headers, requestBody.getText());

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

                FileType fileType = CustomEditor.TEXT_FILE_TYPE;
                // Content-Type
                final String contentType = execute.header(Header.CONTENT_TYPE);
                if (contentType != null) {
                    if (compileRegExp(regJsonContext).matcher(contentType).find()) {
                        fileType = CustomEditor.JSON_FILE_TYPE;
                    } else if (compileRegExp(regHtml).matcher(contentType).find()) {
                        fileType = CustomEditor.HTML_FILE_TYPE;
                    } else if (compileRegExp(regXml).matcher(contentType).find()) {
                        fileType = CustomEditor.XML_FILE_TYPE;
                    }
                }
                final FileType finalFileType = fileType;
                final String responseBody = execute.body();
                application.invokeLater(() -> responseView.setText(responseBody, finalFileType));
            } catch (Exception e) {
                final String response = String.format("%s", e);
                application.invokeLater(() -> responseView.setText(response, CustomEditor.TEXT_FILE_TYPE));
            }
            application.invokeLater(() -> responseView.setPlaceholder(null));
        };
        responseView.setText(null);
        poolExecutor.execute(command);
    }

    @Nullable
    private CustomEditor getCurrentTabbedOfRequest() {
        TabInfo tabInfo = tabs.getSelectedInfo();
        if (tabInfo == null) {
            return null;
        }
        Component component = tabInfo.getComponent();
        if (component instanceof CustomEditor) {
            return (CustomEditor) component;
        }
        return null;
    }

    public void chooseRequest(@Nullable ApiService apiService) {
        this.chooseApiService = apiService;
        this.requestBodyFileType.setSelectedItem(getCacheType());
        this.requestHead.setFileType(apiService == null ? DEFAULT_FILE_TYPE : CustomEditor.JSON_FILE_TYPE);

        Callable<ParseRequest> parseRequestCallable = () -> ParseRequest.wrap(apiService, this);
        Consumer<ParseRequest> parseRequestConsumer = parseRequest -> {
            // 选择Body页面
            tabs.select(bodyTab, false);

            requestMethod.setSelectedItem(parseRequest.getMethod());
            requestUrl.setText(parseRequest.getUrl());
            requestHead.setText(parseRequest.getHead());
            requestBody.setText(parseRequest.getBody());
            responseView.setText(null);
        };
        Async.runRead(project, parseRequestCallable, parseRequestConsumer);
    }

    public void setCallback(DetailHandle callback) {
        this.callback = callback;
    }

    public void reset() {
        this.chooseRequest(null);
    }

    private HttpRequest generateHttpRequest(@NotNull HttpMethod method,
                                            @NotNull String url,
                                            Map<String, Object> head,
                                            String body) {
        HttpRequest request = HttpUtil.createRequest(Method.valueOf(method.name()), url).timeout(REQUEST_TIMEOUT);

        // 添加请求头
        if (head != null && !head.isEmpty()) {
            head.forEach((headerName, value) -> request.header(headerName, (String) value));
        }
        if (body == null || "".equals(body.trim())) {
            return request;
        }

        Map<String, Object> formData = convert.formatMap(body);
        if (formData != null && !convert.isRaw()) {
            formData.forEach(request::form);
        } else {
            String bodyData = formData != null ? convert.formatString(formData) : body;
            if (formData != null && convert.isBasicDataTypes()) {
                bodyData = (String) formData.get(convert.getBasicDataParamName());
            }
            request.body(bodyData, "application/json");
        }

        // 替换url上的变量
        if (formData != null) {
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                url = url.replace("{" + entry.getKey() + "}", String.valueOf(value));
            }
            request.setUrl(url);
        }
        return request;
    }

    @NotNull
    public String getCache(@NotNull String name, @NotNull ApiService apiService) {
        switch (name) {
            case IDENTITY_HEAD:
                return apiService.getHeaders();
            case IDENTITY_BODY:
                String body = bodyCache.getOrDefault(apiService, null);
                if (body == null) {
                    bodyCache.remove(apiService);
                    body = "";
                }
                return body;
            default:
                break;
        }
        return "";
    }

    public void setCache(@NotNull String name, @NotNull ApiService apiService, @NotNull String cache) {
        switch (name) {
            case IDENTITY_HEAD:
                apiService.setHeaders(cache);
                break;
            case IDENTITY_BODY:
                if (cache.equals(bodyCache.get(apiService))) {
                    return;
                }
                bodyCache.put(apiService, cache);
                break;
            default:
                break;
        }
    }

    @NotNull
    public FileType getCacheType() {
        if (chooseApiService == null) {
            return DEFAULT_FILE_TYPE;
        }
        return bodyTextTypeCache.getOrDefault(chooseApiService, CustomEditor.JSON_FILE_TYPE);
    }

    public void setCacheType(@NotNull FileType fileType) {
        if (chooseApiService == null) {
            return;
        }
        bodyTextTypeCache.put(chooseApiService, fileType);
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

    protected static class ParseRequest {

        private HttpMethod method;
        private String url;
        private String head;
        private String body;

        private ParseRequest(HttpMethod method, String url, String head, String body) {
            this.method = method;
            this.url = url;
            this.head = head;
            this.body = body;
        }

        @NotNull
        public static ParseRequest wrap(ApiService apiService, HttpTestPanel detail) {
            HttpMethod selItem = HttpMethod.GET;
            String reqUrl = null;
            String reqHead = null;
            String reqBody = null;
            try {
                if (apiService != null) {
                    reqUrl = apiService.getRequestUrl();

                    selItem = apiService.getMethod() == null || apiService.getMethod() == HttpMethod.REQUEST ?
                            HttpMethod.GET : apiService.getMethod();

                    reqHead = apiService.getHeaders();

                    if (detail.bodyCache.containsKey(apiService)) {
                        reqBody = detail.getCache(IDENTITY_BODY, apiService);
                    } else {
                        detail.convert.setPsiElement(apiService.getPsiElement());
                        reqBody = detail.convert.formatString();
                        detail.setCache(IDENTITY_BODY, apiService, reqBody);
                    }
                }
            } catch (PsiInvalidElementAccessException e) {
                /*
                @Throws Code: request.getPsiMethod().getResolveScope()
                @Throws Message: 无效访问，通常代表指向方法已被删除
                 */
                if (detail.callback != null) {
                    detail.callback.handle();
                }
            }

            return new ParseRequest(selItem, reqUrl, reqHead, reqBody);
        }

        public HttpMethod getMethod() {
            return method;
        }

        public void setMethod(HttpMethod method) {
            this.method = method;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHead() {
            return head;
        }

        public void setHead(String head) {
            this.head = head;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
