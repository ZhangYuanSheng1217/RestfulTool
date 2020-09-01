/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: BooleanConverter
  Author:   ZhangYuanSheng
  Date:     2020/9/2 01:28
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils.xml.converter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class FloatConverter extends BaseConverter<Float> {

    @Nullable
    @Override
    public Float fromString(@NotNull String value) {
        try {
            return Float.parseFloat(value);
        } catch (Exception ignore) {
        }
        return null;
    }
}
