package com.github.restful.tool.utils;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.*;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.beans.settings.Settings;
import com.github.restful.tool.view.components.editor.CustomEditor;
import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * 请求工具类
 *
 * @author ZhangYuanSheng
 * @version 1.0
 */
public final class HttpUtils {

    private static final int REQUEST_TIMEOUT = 1000 * 10;

    private static final ExecutorService executor = ThreadUtil.newSingleExecutor();

    private static final Pattern jsonPattern = Pattern.compile("application/json", Pattern.CASE_INSENSITIVE);
    private static final Pattern htmlPattern = Pattern.compile("text/html", Pattern.CASE_INSENSITIVE);
    private static final Pattern xmlPattern = Pattern.compile("text/xml", Pattern.CASE_INSENSITIVE);

    private HttpUtils() {
        // private
    }

    public static HttpRequest newHttpRequest(@NotNull HttpMethod method,
                                             @NotNull String url,
                                             @NotNull Map<String, Object> headers,
                                             String body) {
        HttpRequest request = HttpUtil
                .createRequest(Method.valueOf(method.name()), url)
                .timeout(REQUEST_TIMEOUT);
        headers.forEach((name, value) -> request.header(name, String.valueOf(value)));

        if (body == null || "".equals(body.trim())) {
            return request;
        }

        // 设置内容主体, 且自动判断类
        request.body(body);

        // 替换url上的变量
        if (body.contains("{") && body.contains("}") && JSONUtil.isJson(body)) {
            JSONObject json = JSONUtil.parseObj(body);
            for (Map.Entry<String, Object> entry : json.entrySet()) {
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
    public static FileType parseFileType(@NotNull HttpResponse response) {
        FileType fileType = CustomEditor.TEXT_FILE_TYPE;
        // Content-Type
        final String contentType = response.header(Header.CONTENT_TYPE);

        if (contentType != null) {
            if (jsonPattern.matcher(contentType).find()) {
                fileType = CustomEditor.JSON_FILE_TYPE;
            } else if (htmlPattern.matcher(contentType).find()) {
                fileType = CustomEditor.HTML_FILE_TYPE;
            } else if (xmlPattern.matcher(contentType).find()) {
                fileType = CustomEditor.XML_FILE_TYPE;
            }
        }
        return fileType;
    }

    public static void run(@NotNull HttpRequest request) {
        run(request, null, null);
    }

    public static void run(@NotNull HttpRequest request,
                           @Nullable Consumer<HttpResponse> onResult) {
        run(request, onResult, null);
    }

    public static void run(@NotNull HttpRequest request,
                           @Nullable Consumer<HttpResponse> onResult,
                           @Nullable Consumer<Exception> onError) {
        run(request, onResult, onError, null);
    }

    public static void run(@NotNull HttpRequest request,
                           @Nullable Consumer<HttpResponse> onResult,
                           @Nullable Consumer<Exception> onError,
                           @Nullable Runnable onComplete) {
        executor.execute(() -> {
            try {
                HttpResponse response = request.execute();
                // 最大重定向的次数
                final int redirectMaxCount = Settings.HttpToolOptionForm.REDIRECT_MAX_COUNT.getData();
                int redirectCount = 0;
                while (redirectCount++ < redirectMaxCount && response.getStatus() == HttpStatus.HTTP_MOVED_TEMP) {
                    String redirect = response.header(Header.LOCATION);
                    request.setUrl(redirect);
                    response = request.execute();
                }

                if (onResult != null) {
                    onResult.accept(response);
                }
            } catch (Exception e) {
                if (onError != null) {
                    onError.accept(e);
                    return;
                }
                throw e;
            } finally {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }
}
