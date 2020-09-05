/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: DefaultConvert
  Author:   ZhangYuanSheng
  Date:     2020/6/4 14:41
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils.convert;

import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class DefaultConvert extends BaseConvert<Object> {

    public DefaultConvert() {
    }

    public DefaultConvert(@NotNull PsiMethod psiMethod) {
        super(psiMethod);
    }

    @Override
    public String formatString() {
        Map<String, Object> methodParams = parseMethodParams();
        StringBuilder sb = new StringBuilder();
        methodParams.forEach((s, o) -> sb.append(s).append(": ").append(o).append("\n"));
        return sb.toString();
    }

    @Override
    public Map<String, Object> formatMap(@NotNull String paramsStr) {
        Map<String, Object> map = new HashMap<>();

        for (String line : paramsStr.split("\n")) {
            String[] items = line.split(":");
            if (items.length == 2) {
                map.put(items[0].trim(), items[1].trim());
            }
        }

        return map;
    }
}
