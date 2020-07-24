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

import com.github.restful.tool.utils.JsonUtil;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class JsonConvert extends BaseConvert<Object> {

    public JsonConvert() {
    }

    public JsonConvert(@NotNull PsiMethod psiMethod) {
        super(psiMethod);
    }

    @Override
    public String formatString() {
        Map<String, Object> methodParams = parseMethodParams();
        return JsonUtil.formatJson(methodParams);
    }

    @Override
    public Map<String, Object> formatMap(@NotNull String paramsStr) {
        //noinspection unchecked
        return (Map<String, Object>) JsonUtil.formatMap(paramsStr);
    }
}
