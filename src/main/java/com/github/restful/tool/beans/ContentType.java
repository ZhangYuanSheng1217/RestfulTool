package com.github.restful.tool.beans;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;

/**
 * 常用Content-Type类型枚举
 *
 * @author looly
 * @since 4.0.11
 */
public enum ContentType {

    /**
     * 标准表单编码，当action为get时候，浏览器用x-www-form-urlencoded的编码方式把form数据转换成一个字串（name1=value1&amp;name2=value2…）
     */
    FORM_URLENCODED("application/x-www-form-urlencoded"),
    /**
     * 文件上传编码，浏览器会把整个表单以控件为单位分割，并为每个部分加上Content-Disposition，并加上分割符(boundary)
     */
    MULTIPART("multipart/form-data"),
    /**
     * Rest请求JSON编码
     */
    JSON("application/json"),
    /**
     * Rest请求XML编码
     */
    XML("application/xml"),
    /**
     * text/plain编码
     */
    TEXT_PLAIN("text/plain"),
    /**
     * Rest请求text/xml编码
     */
    TEXT_XML("text/xml"),
    /**
     * text/html编码
     */
    TEXT_HTML("text/html");

    private final String value;

    ContentType(@NotNull String value) {
        this.value = value;
    }

    /**
     * 输出Content-Type字符串，附带编码信息
     *
     * @param contentType Content-Type类型
     * @param charset     编码
     * @return Content-Type字符串
     * @since 4.5.4
     */
    @NotNull
    public static String build(@NotNull ContentType contentType, @NotNull Charset charset) {
        return String.format("%s,charset=%s", contentType.getValue(), charset.name());
    }

    /**
     * 获取value值
     *
     * @return value值
     * @since 5.2.6
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    /**
     * 输出Content-Type字符串，附带编码信息
     *
     * @param charset 编码
     * @return Content-Type字符串
     */
    @NotNull
    public String toString(Charset charset) {
        return build(this, charset);
    }

    @NotNull
    public static ContentType find(@Nullable String contentType) {
        if (contentType == null) {
            return FORM_URLENCODED;
        }
        for (ContentType type : values()) {
            if (type.getValue().equals(contentType)) {
                return type;
            }
        }
        return FORM_URLENCODED;
    }
}
