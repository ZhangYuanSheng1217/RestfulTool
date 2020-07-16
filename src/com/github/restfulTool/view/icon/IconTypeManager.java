/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: IconManager
  Author:   ZhangYuanSheng
  Date:     2020/5/31 03:40
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restfulTool.view.icon;

import com.intellij.ui.IconManager;
import com.github.restfulTool.beans.HttpMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class IconTypeManager {

    private static final String BASE_PATH = "/icons/method/";

    private static final Map<String, IconType> ICON_TYPES = new HashMap<>();

    static {
        autoScanner();
    }

    private IconTypeManager() {
        // Nothing
    }

    /**
     * 获取图标风格列表
     *
     * @return array
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public static IconType[] getIconTypes() {
        List<IconType> iconTypes = new ArrayList<>();
        for (Map.Entry<String, IconType> entry : ICON_TYPES.entrySet()) {
            iconTypes.add(entry.getValue());
        }
        iconTypes.sort((o1, o2) -> {
            char[] chars1 = o1.toString().toCharArray();
            char[] chars2 = o2.toString().toCharArray();

            int maxLen = Math.min(chars1.length, chars2.length);
            for (int i = 0; i < maxLen; i++) {
                if (chars1[i] != chars2[i]) {
                    return chars1[i] - chars2[i];
                }
            }

            return chars1.length - chars2.length;
        });
        return iconTypes.toArray(new IconType[0]);
    }

    @NotNull
    public static IconType getInstance(@NotNull Object obj) {
        if (obj instanceof IconType) {
            IconType iconType = (IconType) obj;
            if (ICON_TYPES.containsValue(iconType)) {
                return iconType;
            }
            if (ICON_TYPES.containsKey(iconType.toString())) {
                return ICON_TYPES.get(iconType.toString());
            }
        }
        String scheme = obj instanceof String ? ((String) obj) : obj.toString();
        if (!ICON_TYPES.containsKey(scheme)) {
            throw new RuntimeException(scheme + " Scheme notfound");
        }
        return ICON_TYPES.get(scheme);
    }

    /**
     * 扫描 resources/icons/method 下的图标主题
     * <p>
     * 命名方式:<br/>
     * L 文件夹名 --- 图标主题名<br/>
     * L_ GET.[svg|png] --- GET方式请求的默认图标<br/>
     * L_ GET_select.[svg|png] --- GET方式请求的选中图标<br/>
     * </p>
     */
    public static void autoScanner() {
        try {
            URL url = IconType.class.getResource(BASE_PATH);
            File iconsDir = new File(url.toURI());
            assert iconsDir.isDirectory();
            File[] files = iconsDir.listFiles(File::isDirectory);
            assert files != null;
            ICON_TYPES.clear();
            for (File file : files) {
                try {
                    IconType iconType = generateIconType(file);
                    ICON_TYPES.put(iconType.toString(), iconType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private static IconType generateIconType(@NotNull File iconDir) throws Exception {
        String name = iconDir.getName();

        File[] iconsFile = iconDir.listFiles((dir, fileName) -> {
            String temp = fileName.toLowerCase();
            // 只识别 { .svg | .png } 格式的图标
            return temp.endsWith(".svg") || temp.endsWith(".png");
        });
        if (iconsFile == null || iconsFile.length < 1) {
            throw new Exception("Icons must be not empty [.png | .svg]");
        }

        Map<HttpMethod, Icon> defaultIcons = new HashMap<>(9);
        Map<HttpMethod, Icon> selectedIcons = new HashMap<>(9);

        for (File file : iconsFile) {
            String fileName = file.getName();
            Icon icon;
            if (fileName.toLowerCase().endsWith("svg")) {
                icon = IconManager.getInstance().getIcon(
                        BASE_PATH + name + "/" + fileName,
                        IconTypeManager.class
                );
            } else {
                icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(file.toURI().toURL()));
            }
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            if (fileName.contains("_select")) {
                // select
                selectedIcons.put(
                        HttpMethod.parse(fileName.replace("_select", "")),
                        icon
                );
            } else {
                // default
                defaultIcons.put(
                        HttpMethod.parse(fileName),
                        icon
                );
            }
        }
        return generateIconType(name, defaultIcons, selectedIcons);
    }

    @NotNull
    private static IconType generateIconType(
            @NotNull final String name,
            @NotNull final Map<HttpMethod, Icon> defaultIcons,
            @NotNull final Map<HttpMethod, Icon> selectedIcons) {
        return new IconType() {

            @Override
            @NotNull
            public String toString() {
                return name;
            }

            @NotNull
            @Override
            public Icon getDefaultIcon(HttpMethod method) {
                return defaultIcons.get(method);
            }

            @NotNull
            @Override
            public Icon getSelectIcon(HttpMethod method) {
                if (selectedIcons.containsKey(method)) {
                    return selectedIcons.get(method);
                }
                return getDefaultIcon(method);
            }

            @NotNull
            @Override
            public List<PreviewIcon> getDefaultIcons() {
                return getIcons(false);
            }

            @NotNull
            @Override
            public List<PreviewIcon> getSelectIcons() {
                return getIcons(true);
            }

            @NotNull
            private List<PreviewIcon> getIcons(boolean selected) {
                List<PreviewIcon> previewIcons = new ArrayList<>();
                if (!selected) {
                    defaultIcons.forEach((method, icon) -> previewIcons.add(
                            new PreviewIcon(method.name(), icon)
                    ));
                } else {
                    if (selectedIcons.size() == defaultIcons.size()) {
                        selectedIcons.forEach((method, icon) -> previewIcons.add(
                                new PreviewIcon(method.name(), icon)
                        ));
                    } else {
                        defaultIcons.forEach((method, icon) -> {
                            PreviewIcon previewIcon = new PreviewIcon(method.name(), icon);
                            if (selectedIcons.containsKey(method)) {
                                previewIcon.setIcon(selectedIcons.get(method));
                            }
                            previewIcons.add(previewIcon);
                        });
                    }
                }
                previewIcons.sort(new IconComparator());
                return previewIcons;
            }
        };
    }
}
