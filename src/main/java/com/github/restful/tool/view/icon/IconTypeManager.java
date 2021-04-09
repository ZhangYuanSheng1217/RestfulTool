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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public final class IconTypeManager {

    private static final Map<String, IconType> ICON_TYPES = new HashMap<>();

    static {
        DefaultIconType defaultIconType = new DefaultIconType();
        ICON_TYPES.put(defaultIconType.toString(), defaultIconType);

        CuteIconType cuteIconType = new CuteIconType();
        ICON_TYPES.put(cuteIconType.toString(), cuteIconType);
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
        String scheme = Objects.toString(obj);
        return ICON_TYPES.get(scheme);
    }
}
