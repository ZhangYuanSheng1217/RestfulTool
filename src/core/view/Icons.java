/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: Icons
  Author:   ZhangYuanSheng
  Date:     2020/5/6 10:39
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package core.view;

import com.intellij.ui.IconManager;
import com.intellij.ui.components.JBLabel;
import core.beans.RequestMethod;
import core.configuration.AppSettingsState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class Icons {

    public static final Icon SERVICE = load("/icons/service.png");

    public static final Icon METHOD_ALL = load("/icons/method/Request.svg");
    public static final Icon METHOD_ALL_SELECT = load("/icons/method/Request_select.svg");
    public static final Icon METHOD_ALL_OLD = load("/icons/method/old/Request.png");

    public static final Icon METHOD_GET = load("/icons/method/GET.svg");
    public static final Icon METHOD_GET_SELECT = load("/icons/method/GET_select.svg");
    public static final Icon METHOD_GET_OLD = load("/icons/method/old/GET.png");

    public static final Icon METHOD_POST = load("/icons/method/POST.svg");
    public static final Icon METHOD_POST_SELECT = load("/icons/method/POST_select.svg");
    public static final Icon METHOD_POST_OLD = load("/icons/method/old/POST.png");

    public static final Icon METHOD_DELETE = load("/icons/method/DELETE.svg");
    public static final Icon METHOD_DELETE_SELECT = load("/icons/method/DELETE_select.svg");
    public static final Icon METHOD_DELETE_OLD = load("/icons/method/old/DELETE.png");

    public static final Icon METHOD_PUT = load("/icons/method/PUT.svg");
    public static final Icon METHOD_PUT_SELECT = load("/icons/method/PUT_select.svg");
    public static final Icon METHOD_PUT_OLD = load("/icons/method/old/PUT.png");

    public static final Icon METHOD_PATCH = load("/icons/method/PATCH.svg");
    public static final Icon METHOD_PATCH_SELECT = load("/icons/method/PATCH_select.svg");
    public static final Icon METHOD_PATCH_OLD = load("/icons/method/old/PATCH.png");

    @NotNull
    public static Icon load(@NotNull String path) {
        return IconManager.getInstance().getIcon(path, Icons.class);
    }

    /**
     * 获取方法对应的图标
     *
     * @param method 请求类型
     * @return icon
     */
    @NotNull
    public static Icon getMethodIcon(RequestMethod method) {
        return getMethodIcon(method, false);
    }

    public static Icon getSelectIcon(RequestMethod method) {
        if (AppSettingsState.getInstance().getAppSetting().useOldIcons) {
            return getMethodIcon(method);
        }
        return getMethodIcon(method, true);
    }

    public static Icon getMethodIcon(RequestMethod method, boolean selected) {
        boolean useOldIcons = AppSettingsState.getInstance().getAppSetting().useOldIcons;

        if (method == null) {
            if (useOldIcons) {
                return Icons.METHOD_ALL_OLD;
            }
            return selected ? Icons.METHOD_ALL_SELECT : Icons.METHOD_ALL;
        }
        switch (method) {
            case GET:
                if (useOldIcons) {
                    return Icons.METHOD_GET_OLD;
                }
                return selected ? Icons.METHOD_GET_SELECT : Icons.METHOD_GET;
            case POST:
                if (useOldIcons) {
                    return Icons.METHOD_POST_OLD;
                }
                return selected ? Icons.METHOD_POST_SELECT : Icons.METHOD_POST;
            case DELETE:
                if (useOldIcons) {
                    return Icons.METHOD_DELETE_OLD;
                }
                return selected ? Icons.METHOD_DELETE_SELECT : Icons.METHOD_DELETE;
            case PUT:
                if (useOldIcons) {
                    return Icons.METHOD_PUT_OLD;
                }
                return selected ? Icons.METHOD_PUT_SELECT : Icons.METHOD_PUT;
            case PATCH:
                if (useOldIcons) {
                    return Icons.METHOD_PATCH_OLD;
                }
                return selected ? Icons.METHOD_PATCH_SELECT : Icons.METHOD_PATCH;
            default:
                if (useOldIcons) {
                    return Icons.METHOD_ALL_OLD;
                }
                return selected ? Icons.METHOD_ALL_SELECT : Icons.METHOD_ALL;
        }
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static PreviewIcon[] getAllIcons(boolean withOld) {
        return new PreviewIcon[]{
                new PreviewIcon("Request", withOld ? Icons.METHOD_ALL_OLD : Icons.METHOD_ALL),
                new PreviewIcon("Get", withOld ? METHOD_GET_OLD : Icons.METHOD_GET),
                new PreviewIcon("Post", withOld ? METHOD_POST_OLD : Icons.METHOD_POST),
                new PreviewIcon("Delete", withOld ? METHOD_DELETE_OLD : Icons.METHOD_DELETE),
                new PreviewIcon("Put", withOld ? METHOD_PUT_OLD : Icons.METHOD_PUT),
                new PreviewIcon("Patch", withOld ? METHOD_PATCH_OLD : Icons.METHOD_PATCH),
        };
    }

    @Nullable
    public static PreviewIcon[] getAllSelectIcons(boolean withOld) {
        if (withOld) {
            return null;
        }
        return new PreviewIcon[]{
                new PreviewIcon("Request", METHOD_ALL_SELECT),
                new PreviewIcon("Get", METHOD_GET_SELECT),
                new PreviewIcon("Post", METHOD_POST_SELECT),
                new PreviewIcon("Delete", METHOD_DELETE_SELECT),
                new PreviewIcon("Put", METHOD_PUT_SELECT),
                new PreviewIcon("Patch", METHOD_PATCH_SELECT),
        };
    }

    public static class PreviewIcon extends JBLabel {

        public PreviewIcon(@NotNull String text, @NotNull Icon icon) {
            super(text, icon, CENTER);
        }
    }
}
