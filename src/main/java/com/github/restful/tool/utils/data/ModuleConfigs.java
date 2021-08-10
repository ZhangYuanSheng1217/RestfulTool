package com.github.restful.tool.utils.data;

import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.utils.data.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author ZhangYuanSheng
 */
public class ModuleConfigs {

    private ModuleConfigs() {
        // private
    }

    @NotNull
    public static Map<String, String> getModuleConfig(@NotNull Project project, @NotNull String moduleName) {
        return Storage.MODULE_HTTP_CONFIG.getMap(project, moduleName);
    }

    @NotNull
    public static String getModuleConfig(@NotNull Project project, @NotNull String moduleName, @NotNull Config config) {
        return config.getConfig(project, moduleName);
    }

    public static void setModuleConfig(@NotNull Project project, @NotNull String moduleName, @NotNull Map<String, String> config) {
        Storage.MODULE_HTTP_CONFIG.setValue(project, moduleName, config);
    }

    public static void resetModuleConfig(@NotNull Project project, @NotNull String moduleName) {
        Storage.MODULE_HTTP_CONFIG.setValue(project, moduleName, (Map<String, String>) null);
    }

    public enum Config {

        PORT("port"),

        CONTEXT_PATH("context-path"),

        ;

        private final String name;

        Config(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @NotNull
        public String getConfig(@NotNull Project project, @NotNull String moduleName) {
            return Storage.MODULE_HTTP_CONFIG.getMap(project, moduleName).getOrDefault(getName(), "");
        }

        public static void apply(@NotNull Map<String, String> config, @NotNull ApiService apiService) {
            String port = config.get(PORT.getName());
            apiService.setPort(port);

            String contextPath = config.get(CONTEXT_PATH.getName());
            if (contextPath != null) {
                apiService.setPath(contextPath + apiService.getPath());
            }
        }
    }
}