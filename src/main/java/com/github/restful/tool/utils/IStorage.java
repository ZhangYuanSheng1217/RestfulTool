package com.github.restful.tool.utils;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface IStorage {

    static PropertiesComponent getInstance(@Nullable Project project) {
        if (project == null) {
            return PropertiesComponent.getInstance();
        }
        return PropertiesComponent.getInstance(project);
    }

    @NotNull
    String getName();

    /*
    ------------------------------- String:Start -------------------------------
     */

    default String getString() {
        return getInstance(null).getValue(getName());
    }

    default String getString(@NotNull Project project) {
        return getInstance(project).getValue(getName());
    }

    @NotNull
    default String getString(@NotNull String defaultValue) {
        return getInstance(null).getValue(getName(), defaultValue);
    }

    @NotNull
    default String getString(@NotNull Project project, @NotNull String defaultValue) {
        return getInstance(project).getValue(getName(), defaultValue);
    }

    /*
    ------------------------------- String:End -------------------------------
     */

    /*
    ------------------------------- Boolean:Start -------------------------------
     */

    default boolean getBoolean() {
        return getInstance(null).getBoolean(getName());
    }

    default boolean getBoolean(@NotNull Project project) {
        return getInstance(project).getBoolean(getName());
    }

    default boolean getBoolean(boolean defaultValue) {
        return getInstance(null).getBoolean(getName(), defaultValue);
    }

    default boolean getBoolean(@NotNull Project project, boolean defaultValue) {
        return getInstance(project).getBoolean(getName(), defaultValue);
    }

    /*
    ------------------------------- Boolean:End -------------------------------
     */

    /*
    ------------------------------- Integer:Start -------------------------------
     */

    @NotNull
    default Integer getInteger(int defaultValue) {
        return getInstance(null).getInt(getName(), defaultValue);
    }

    @NotNull
    default Integer getInteger(@NotNull Project project, int defaultValue) {
        return getInstance(project).getInt(getName(), defaultValue);
    }

    /*
    ------------------------------- Integer:End -------------------------------
     */

    /*
    ------------------------------- setValue:Start -------------------------------
     */

    default void setValue(@Nullable String value) {
        getInstance(null).setValue(getName(), value);
    }

    default void setValue(@NotNull Project project, @Nullable String value) {
        getInstance(project).setValue(getName(), value);
    }

    default void setValue(@Nullable String value, @Nullable String defaultValue) {
        getInstance(null).setValue(getName(), value, defaultValue);
    }

    default void setValue(@NotNull Project project, @Nullable String value, @Nullable String defaultValue) {
        getInstance(project).setValue(getName(), value, defaultValue);
    }

    default void setValue(boolean value) {
        getInstance(null).setValue(getName(), value);
    }

    default void setValue(@NotNull Project project, boolean value) {
        getInstance(project).setValue(getName(), value);
    }

    default void setValue(boolean value, boolean defaultValue) {
        getInstance(null).setValue(getName(), value, defaultValue);
    }

    default void setValue(@NotNull Project project, boolean value, boolean defaultValue) {
        getInstance(project).setValue(getName(), value, defaultValue);
    }

    default void setValue(int value, int defaultValue) {
        getInstance(null).setValue(getName(), value, defaultValue);
    }

    default void setValue(@NotNull Project project, int value, int defaultValue) {
        getInstance(project).setValue(getName(), value, defaultValue);
    }

    /*
    ------------------------------- setValue:End -------------------------------
     */
}
