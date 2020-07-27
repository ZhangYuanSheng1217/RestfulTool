/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: JsonUtils
  Author:   ZhangYuanSheng
  Date:     2020/7/23 11:25
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class JsonUtil {

    private JsonUtil() {
    }

    @Nullable
    public static String formatJson(@NotNull Map<?, ?> map) {
        return formatJson(map, true);
    }

    @Nullable
    public static String formatJson(@NotNull Map<?, ?> map, boolean pretty) {
        try {
            ObjectMapper mapper = Ops.OBJECT_MAPPER;
            if (pretty) {
                return mapper.writer(Ops.PRETTY_PRINTER).writeValueAsString(map);
            }
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Nullable
    public static Map<?, ?> formatMap(@NotNull String json) {
        return formatObject(json, Map.class);
    }

    @Nullable
    public static <T> T formatObject(@NotNull String json, Class<T> clazz) {
        try {
            return Ops.OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static final class Ops {
        public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        public static final DefaultPrettyPrinter PRETTY_PRINTER = new CustomPrettyPrinter();

        private static final class CustomPrettyPrinter extends DefaultPrettyPrinter {

            private CustomPrettyPrinter() {
                super._objectFieldValueSeparatorWithSpaces = ": ";
            }

            @NotNull
            @Override
            public DefaultPrettyPrinter createInstance() {
                return this;
            }

            @Override
            public void writeEndObject(JsonGenerator g, int nrOfEntries) throws IOException {
                if (!_objectIndenter.isInline()) {
                    --_nesting;
                }
                if (nrOfEntries > 0) {
                    _objectIndenter.writeIndentation(g, _nesting);
                }

                g.writeRaw('}');
            }

            @Override
            public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
                if (!_arrayIndenter.isInline()) {
                    --_nesting;
                }
                if (nrOfValues > 0) {
                    _arrayIndenter.writeIndentation(g, _nesting);
                }

                g.writeRaw(']');
            }
        }
    }
}
