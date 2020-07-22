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
package com.github.restful.tool.view.icon;

import com.github.restful.tool.beans.HttpMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public final class IconTypeManager {

    public static final String DEFAULT_ICON_SCHEME = "default";
    private static final String ICON_SCHEME_PATH = "icons/method/";
    private static final Map<String, IconType> ICON_TYPES = new ConcurrentHashMap<>();

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

            for (int i = 0; i < Math.min(chars1.length, chars2.length); i++) {
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
            return ICON_TYPES.get(DEFAULT_ICON_SCHEME);
        }
        return ICON_TYPES.get(scheme);
    }

    /**
     * 扫描 resources/icons/method 下的图标主题
     * <p>
     * 命名方式:<br/>
     * 文件夹名 --- 图标主题名<br/>
     * |--- GET.[svg|png] --- GET方式请求的默认图标<br/>
     * |--- GET_select.[svg|png] --- GET方式请求的选中图标<br/>
     * </p>
     */
    public static void autoScanner() {
        URL url = IconTypeManager.class.getClassLoader().getResource(ICON_SCHEME_PATH);
        if (url == null) {
            url = IconTypeManager.class.getResource(ICON_SCHEME_PATH);
        }
        String urlStr = url.toString();
        String jarPath = urlStr.substring(0, urlStr.indexOf("!/") + 2);
        Map<String, List<String>> directory = readDirectoryFromPath(jarPath);
        if (!directory.isEmpty()) {
            ICON_TYPES.clear();
        }
        directory.forEach((name, paths) -> {
            IconType iconType = generateIconType(name, paths);
            if (!DEFAULT_ICON_SCHEME.equals(name)) {
                ICON_TYPES.put(name, iconType);
            }
        });
        ICON_TYPES.put(DEFAULT_ICON_SCHEME, new DefaultIconType());
    }

    /**
     * 扫描jar包下指定文件夹的文件列表
     *
     * @param jarPath jar包的绝对定位
     * @return map
     */
    @NotNull
    private static Map<String, List<String>> readDirectoryFromPath(@NotNull String jarPath) {
        Map<String, List<String>> map = new HashMap<>();
        try {
            if (!jarPath.endsWith("!/")) {
                jarPath += "!/";
            }
            URL jarUrl = new URL(jarPath);
            JarURLConnection jarCon = (JarURLConnection) jarUrl.openConnection();
            JarFile jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntry = jarFile.entries();
            while (jarEntry.hasMoreElements()) {
                JarEntry entry = jarEntry.nextElement();
                String fullName = entry.getName();
                if (fullName.equals(IconTypeManager.ICON_SCHEME_PATH) || !fullName.startsWith(IconTypeManager.ICON_SCHEME_PATH)) {
                    continue;
                }
                String currName = fullName.replace(IconTypeManager.ICON_SCHEME_PATH, "");
                if (entry.isDirectory()) {
                    if (currName.endsWith("/")) {
                        currName = currName.substring(0, currName.lastIndexOf("/"));
                    }
                    map.put(currName, new ArrayList<>());
                } else if (fullName.toLowerCase().endsWith(".svg") || fullName.toLowerCase().endsWith(".png")) {
                    String sub = currName.substring(0, currName.indexOf("/"));
                    List<String> list = map.get(sub);
                    if (list != null) {
                        list.add(currName);
                    }
                }
            }
        } catch (IOException e) {
            // jar包已经被解压
            try {
                URL url = IconType.class.getResource("/" + IconTypeManager.ICON_SCHEME_PATH);
                File iconsDir = new File(url.toURI());
                assert iconsDir.isDirectory();
                File[] files = iconsDir.listFiles(File::isDirectory);
                assert files != null;
                map.clear();
                for (File file : files) {
                    String[] icons = file.list();
                    if (icons == null) {
                        continue;
                    }
                    String name = file.getName();
                    List<String> iconPaths = new ArrayList<>(icons.length);
                    for (String fileName : icons) {
                        if (fileName.toLowerCase().endsWith(".svg") || fileName.toLowerCase().endsWith(".png")) {
                            iconPaths.add(name + "/" + fileName);
                        }
                    }
                    map.put(name, iconPaths);
                }
            } catch (Exception ignore) {
            }
        }
        return map;
    }

    /**
     * 生成IconScheme的IconType实现类
     *
     * @param name  主题名
     * @param icons 图标相对地址
     * @return IconTypeImpl
     */
    @NotNull
    private static IconType generateIconType(@NotNull String name, @NotNull List<String> icons) {
        HttpMethod[] httpMethods = HttpMethod.values();

        Map<HttpMethod, Icon> defaultIcons = new HashMap<>(httpMethods.length);
        Map<HttpMethod, Icon> selectedIcons = new HashMap<>(httpMethods.length);

        for (String iconPath : icons) {
            String fileName = iconPath.substring(iconPath.indexOf("/") + 1, iconPath.lastIndexOf("."));
            Icon icon = Icons.load("/" + ICON_SCHEME_PATH + iconPath);
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

        if (defaultIcons.isEmpty() || defaultIcons.size() < httpMethods.length) {
            for (HttpMethod method : httpMethods) {
                if (!defaultIcons.containsKey(method)) {
                    defaultIcons.put(method, null);
                }
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
