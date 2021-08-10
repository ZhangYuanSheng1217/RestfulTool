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

import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.service.Notify;
import com.github.restful.tool.service.topic.RestDetailTopic;
import com.github.restful.tool.utils.Async;
import com.github.restful.tool.utils.HttpUtils;
import com.github.restful.tool.utils.convert.ParamsConvert;
import com.github.restful.tool.utils.data.Bundle;
import com.github.restful.tool.utils.data.JsonUtil;
import com.github.restful.tool.view.components.editor.CustomEditor;
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
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class HttpTestPanel extends JPanel {

    public static final FileType DEFAULT_FILE_TYPE = CustomEditor.TEXT_FILE_TYPE;

    private static final String IDENTITY_HEAD = "HEAD";
    private static final String IDENTITY_BODY = "BODY";

    private final transient Project project;

    private final transient Map<ApiService, String> bodyCache;
    private final transient Map<ApiService, FileType> bodyTextTypeCache;

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
    private transient JBTabs tabs;

    /**
     * 文本域 - 请求头
     */
    private transient TabInfo headTab;
    private CustomEditor requestHead;

    /**
     * 文本域 - 请求体
     */
    private transient TabInfo bodyTab;
    private CustomEditor requestBody;
    private ComboBox<FileType> requestBodyFileType;

    /**
     * 标签 - 显示返回结果
     */
    private transient TabInfo responseTab;
    private CustomEditor responseView;

    private transient DetailHandle callback;

    /**
     * 选中的Request
     */
    private transient ApiService chooseApiService;

    public HttpTestPanel(@NotNull Project project) {
        this.project = project;

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
        add(bodyFileTypePanel, BorderLayout.SOUTH);
        tabs.addListener(new TabsListener() {
            @Override
            public void beforeSelectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                bodyFileTypePanel.setVisible(bodyTab.getText().equalsIgnoreCase(newSelection.getText()));
            }
        });

        TabInfo selectedTab = tabs.getSelectedInfo();
        if (selectedTab == null) {
            bodyFileTypePanel.setVisible(false);
        } else {
            bodyFileTypePanel.setVisible(bodyTab.getText().equalsIgnoreCase(selectedTab.getText()));
        }
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
        //noinspection unchecked
        Map<String, Object> headers = (Map<String, Object>) JsonUtil.formatMap(head);
        if (headers == null) {
            // 选择Header页面
            tabs.select(headTab, true);
            Notify.getInstance(project).error("Incorrect request header format!");
            return;
        }

        responseView.setText(null);
        HttpUtils.run(
                HttpUtils.newHttpRequest(method, url, headers, requestBody.getText()),
                response -> {
                    final FileType fileType = HttpUtils.parseFileType(response);
                    final String responseBody = response.body();
                    ApplicationManager.getApplication().invokeLater(
                            () -> responseView.setText(responseBody, fileType)
                    );
                },
                e -> {
                    final String response = String.format("%s", e);
                    ApplicationManager.getApplication().invokeLater(
                            () -> responseView.setText(response, CustomEditor.TEXT_FILE_TYPE)
                    );
                }
        );
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

    public interface DetailHandle {

        /**
         * 处理逻辑
         */
        void handle();
    }

    protected static class ParseRequest {

        private final HttpMethod method;
        private final String url;
        private final String head;
        private final String body;

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
                        reqBody = ParamsConvert.formatString(apiService.getPsiElement());
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

        public String getUrl() {
            return url;
        }

        public String getHead() {
            return head;
        }

        public String getBody() {
            return body;
        }
    }
}
