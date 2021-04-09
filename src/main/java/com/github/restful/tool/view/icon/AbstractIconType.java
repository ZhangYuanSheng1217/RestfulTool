package com.github.restful.tool.view.icon;

import com.github.restful.tool.beans.HttpMethod;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public abstract class AbstractIconType extends IconType {

    private final String name;

    private final Map<HttpMethod, Icon> defaultIcons;
    private final Map<HttpMethod, Icon> selectIcons;

    protected AbstractIconType(@NotNull String name, @NotNull String suffix) {
        this(name, "_select", suffix);
    }

    protected AbstractIconType(@NotNull String name, @NotNull String selectSuffix, @NotNull String suffix) {
        this.name = name;
        this.defaultIcons = new HashMap<>(HttpMethod.values().length);
        this.selectIcons = new HashMap<>(HttpMethod.values().length);

        for (HttpMethod httpMethod : HttpMethod.values()) {
            String iconPath = String.format("/icons/method/%s/%s.%s", name, httpMethod.name(), suffix);
            String iconSelectPath = String.format("/icons/method/%s/%s%s.%s", name, httpMethod.name(), selectSuffix, suffix);
            Icon defaultIcon = Icons.load(iconPath);
            defaultIcons.put(httpMethod, defaultIcon);
            Icon selectIcon;
            try {
                selectIcon = Icons.load(iconSelectPath);
            } catch (Exception e) {
                selectIcon = defaultIcon;
            }
            selectIcons.put(httpMethod, selectIcon);
        }
    }

    @Override
    public final @NotNull Icon getDefaultIcon(HttpMethod method) {
        return defaultIcons.get(method);
    }

    @Override
    public final @NotNull Icon getSelectIcon(HttpMethod method) {
        return selectIcons.get(method);
    }

    @Override
    public final @NotNull List<PreviewIcon> getDefaultIcons() {
        List<PreviewIcon> list = new ArrayList<>(defaultIcons.size());
        defaultIcons.forEach((method, icon) -> list.add(new PreviewIcon(method.name(), icon)));
        return list;
    }

    @Override
    public final @NotNull List<PreviewIcon> getSelectIcons() {
        List<PreviewIcon> list = new ArrayList<>(selectIcons.size());
        selectIcons.forEach((method, icon) -> list.add(new PreviewIcon(method.name(), icon)));
        return list;
    }

    @Override
    public final @NotNull String toString() {
        return this.name;
    }
}
