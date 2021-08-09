package com.github.restful.tool.view.window.frame;

import com.github.restful.tool.utils.JsonUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.Map;

public class ModuleUrlStorageUtil {

    private static final String CONFIG_KEY = "RestfulTool:moduleUrlMap";

    public static Map<String, String> getModuleUrlMap(Project project) {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        String configJson = properties.getValue(CONFIG_KEY, "{}");
        Map<String, String> moduleNameUrlMap = JsonUtil.formatObject(configJson, Map.class);
        if (moduleNameUrlMap == null) {
            return new HashMap<>();
        }
        return moduleNameUrlMap;
    }

    public static void storeModuleUrlMap(Project project, Map<String, String> moduleUrlMap) {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        properties.setValue(CONFIG_KEY, JsonUtil.formatJson(moduleUrlMap));
    }
}