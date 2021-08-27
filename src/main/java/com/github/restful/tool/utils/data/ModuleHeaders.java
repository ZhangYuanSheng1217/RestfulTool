package com.github.restful.tool.utils.data;

import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.beans.settings.Settings;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 */
public class ModuleHeaders {

    private ModuleHeaders() {
        // private
    }

    @NotNull
    public static Map<String, String> getModuleHeader(@NotNull Project project, @NotNull String moduleName) {
        Map<String, String> map = Storage.MODULE_HTTP_HEADER.getMap(project, moduleName);
        if (map.isEmpty()) {
            fillDefault(map);
        }
        return map;
    }

    @NotNull
    public static String getModuleHeader(@NotNull Project project, @NotNull String moduleName, @NotNull String headerName) {
        return Storage.MODULE_HTTP_HEADER.getMap(project, moduleName).getOrDefault(headerName, "");
    }

    public static void setModuleHeader(@NotNull Project project, @NotNull String moduleName, @NotNull Map<String, String> headers) {
        Storage.MODULE_HTTP_HEADER.setValue(project, moduleName, headers);
    }

    public static void resetModuleHeader(@NotNull Project project, @NotNull String moduleName) {
        Map<String, String> headers = new HashMap<>();
        fillDefault(headers);
        Storage.MODULE_HTTP_HEADER.setValue(project, moduleName, headers);
    }

    public static void fillDefault(@NotNull Map<String, String> headers) {
        headers.put("Content-Type", Settings.HttpToolOptionForm.CONTENT_TYPE.getData().getValue());
    }

    public static void apply(@NotNull Map<String, String> headers, @NotNull ApiService apiService) {
        apiService.setModuleHeaders(headers);
    }
}