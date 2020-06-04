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
package core.view.window.frame;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import core.beans.HttpMethod;
import core.beans.Request;
import core.utils.RestUtil;
import core.utils.convert.BaseConvert;
import core.utils.convert.DefaultConvert;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXEditorPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestDetail extends JPanel {

    private static final int REQUEST_TIMEOUT = 1000 * 10;

    private final Project project;
    private final ThreadPoolExecutor poolExecutor;
    private final BaseConvert<?> convert;

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
    private JTabbedPane tabbedPane;
    /**
     * 文本域 - 请求头
     */
    private JEditorPane requestHead;
    /**
     * 文本域 - 请求体
     */
    private JEditorPane requestBody;
    /**
     * 标签 - 显示返回结果
     */
    private JLabel responseView;

    private DetailHandle callback;

    public RestDetail(@NotNull Project project) {
        this.project = project;
        poolExecutor = new ThreadPoolExecutor(
                1,
                1,
                1000,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(8),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        this.convert = new DefaultConvert();

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

        sendRequest = new JXButton("send");
        panelInput.add(sendRequest, BorderLayout.EAST);

        tabbedPane = new JBTabbedPane(JTabbedPane.TOP);
        add(tabbedPane, BorderLayout.CENTER);

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
        // 发送请求按钮监听
        sendRequest.addActionListener(event -> {
            // 选择Response页面
            tabbedPane.setSelectedIndex(2);

            HttpMethod method = (HttpMethod) requestMethod.getSelectedItem();
            if (method == null) {
                method = HttpMethod.GET;
            }
            String url = requestUrl.getText();

            if (url == null || "".equals(url.trim())) {
                responseView.setText("request path must be not empty!");
                return;
            }

            String head = requestHead.getText();
            String body = requestBody.getText();

            HttpRequest httpRequest = getHttpRequest(method, url, head, body);

            responseView.setText(" -> request thread is running");
            poolExecutor.execute(() -> {
                String resp;
                try {
                    resp = httpRequest.execute().body();
                } catch (Exception e) {
                    e.printStackTrace();
                    resp = e.getMessage();
                }
                responseView.setText(resp);
            });
        });
    }

    public void setRequest(Request request) {
        HttpMethod selItem = HttpMethod.GET;
        String reqUrl = "";
        String reqBody = "";

        try {
            if (request != null) {
                GlobalSearchScope scope = request.getPsiMethod().getResolveScope();
                reqUrl = RestUtil.getRequestUrl(
                        RestUtil.scanListenerProtocol(project, scope),
                        RestUtil.scanListenerPort(project, scope),
                        RestUtil.scanContextPath(project, scope),
                        request.getPath()
                );

                // 选择Body页面
                tabbedPane.setSelectedIndex(1);

                selItem = request.getMethod() == null || request.getMethod() == HttpMethod.REQUEST ?
                        HttpMethod.GET : request.getMethod();

                convert.setPsiMethod(request.getPsiMethod());
                reqBody = convert.formatString();
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
        requestBody.setText(reqBody);
    }

    public void setCallback(DetailHandle callback) {
        this.callback = callback;
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
                    requestBody = new JSONObject(formatMap).toString();
                }
                request.body(requestBody, "application/json");
            }
        }
        return request.timeout(REQUEST_TIMEOUT);
    }

    public interface DetailHandle {

        /**
         * 处理逻辑
         */
        void handle();
    }
}
