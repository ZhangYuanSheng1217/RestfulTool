package com.github.restful.tool.beans;

import com.github.restful.tool.beans.settings.Settings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public enum PropertiesKey {

    /**
     * 扫描service时包括lib
     */
    SCAN_SERVICE_WITH_LIB("SCAN_SERVICE_WITH_LIB");

    private final String value;

    PropertiesKey(String value) {
        this.value = value;
    }

    public static boolean scanServiceWithLibrary(@NotNull Project project) {
        PropertiesComponent instance = PropertiesComponent.getInstance(project);
        String value = instance.getValue(
                SCAN_SERVICE_WITH_LIB.value,
                Settings.SystemOptionForm.SCAN_WITH_LIBRARY.getData().toString()
        );
        return Boolean.parseBoolean(value);
    }

    public static void scanServiceWithLibrary(@NotNull Project project, boolean flag) {
        PropertiesComponent.getInstance(project).setValue(SCAN_SERVICE_WITH_LIB.value, flag + "");
        topicNotice(project);
    }

    private static void topicNotice(@NotNull Project project) {

    }
}
