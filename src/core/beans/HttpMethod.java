/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RequestMethod
  Author:   ZhangYuanSheng
  Date:     2020/5/2 00:54
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package core.beans;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public enum HttpMethod {

    /**
     * Request
     */
    REQUEST,

    /**
     * GET
     */
    GET,

    /**
     * OPTIONS
     */
    OPTIONS,

    /**
     * POST
     */
    POST,

    /**
     * PUT
     */
    PUT,

    /**
     * DELETE
     */
    DELETE,

    /**
     * PATCH
     */
    PATCH,

    /**
     * HEAD
     */
    HEAD;

    @NotNull
    public static HttpMethod[] getValues() {
        return Arrays.stream(HttpMethod.values()).filter(method -> !method.equals(HttpMethod.REQUEST)).toArray(HttpMethod[]::new);
    }
}
