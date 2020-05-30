/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: DefaultIcon
  Author:   ZhangYuanSheng
  Date:     2020/5/31 01:20
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package core.view.icon.impl;

import core.beans.HttpMethod;
import core.view.icon.IconType;
import core.view.icon.PreviewIcon;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.view.icon.Icons.load;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class CuteIcon implements IconType {

    public static final Icon REQUEST = load("/icons/method/cute/Request.svg");
    public static final Icon REQUEST_SELECT = load("/icons/method/cute/Request_select.svg");

    public static final Icon GET = load("/icons/method/cute/GET.svg");
    public static final Icon GET_SELECT = load("/icons/method/cute/GET_select.svg");

    public static final Icon POST = load("/icons/method/cute/POST.svg");
    public static final Icon POST_SELECT = load("/icons/method/cute/POST_select.svg");

    public static final Icon DELETE = load("/icons/method/cute/DELETE.svg");
    public static final Icon DELETE_SELECT = load("/icons/method/cute/DELETE_select.svg");

    public static final Icon PUT = load("/icons/method/cute/PUT.svg");
    public static final Icon PUT_SELECT = load("/icons/method/cute/PUT_select.svg");

    public static final Icon PATCH = load("/icons/method/cute/PATCH.svg");
    public static final Icon PATCH_SELECT = load("/icons/method/cute/PATCH_select.svg");

    public static final Icon HEAD = load("/icons/method/cute/HEAD.svg");
    public static final Icon HEAD_SELECT = load("/icons/method/cute/HEAD_select.svg");

    private static final Map<HttpMethod, Icon> ICONS;
    private static final Map<HttpMethod, Icon> ICONS_SELECT;

    static {
        ICONS = new HashMap<>(HttpMethod.values().length);
        ICONS.put(HttpMethod.REQUEST, REQUEST);
        ICONS.put(HttpMethod.GET, GET);
        ICONS.put(HttpMethod.POST, POST);
        ICONS.put(HttpMethod.DELETE, DELETE);
        ICONS.put(HttpMethod.PUT, PUT);
        ICONS.put(HttpMethod.PATCH, PATCH);
        ICONS.put(HttpMethod.HEAD, HEAD);

        ICONS_SELECT = new HashMap<>(HttpMethod.values().length);
        ICONS_SELECT.put(HttpMethod.REQUEST, REQUEST_SELECT);
        ICONS_SELECT.put(HttpMethod.GET, GET_SELECT);
        ICONS_SELECT.put(HttpMethod.POST, POST_SELECT);
        ICONS_SELECT.put(HttpMethod.DELETE, DELETE_SELECT);
        ICONS_SELECT.put(HttpMethod.PUT, PUT_SELECT);
        ICONS_SELECT.put(HttpMethod.PATCH, PATCH_SELECT);
        ICONS_SELECT.put(HttpMethod.HEAD, HEAD_SELECT);
    }

    @NotNull
    @Override
    public Icon getDefaultIcon(@Nullable HttpMethod method) {
        if (!ICONS.containsKey(method)) {
            return REQUEST;
        }
        return ICONS.get(method);
    }

    @NotNull
    @Override
    public Icon getSelectIcon(HttpMethod method) {
        if (!ICONS_SELECT.containsKey(method)) {
            return REQUEST_SELECT;
        }
        return ICONS_SELECT.get(method);
    }

    @NotNull
    @Override
    public List<PreviewIcon> getDefaultIcons() {
        List<PreviewIcon> list = new ArrayList<>(ICONS.size());
        ICONS.forEach((method, icon) -> list.add(new PreviewIcon(method.name(), icon)));
        return list;
    }

    @NotNull
    @Override
    public List<PreviewIcon> getSelectIcons() {
        List<PreviewIcon> list = new ArrayList<>(ICONS_SELECT.size());
        ICONS_SELECT.forEach((method, icon) -> list.add(new PreviewIcon(method.name(), icon)));
        return list;
    }

    @NotNull
    @Override
    public String toString() {
        return "Cute";
    }
}
