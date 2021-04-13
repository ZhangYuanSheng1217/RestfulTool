package com.github.restful.tool.annotation;

import com.github.restful.tool.beans.HttpMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 */
public enum SpringHttpMethodAnnotation {

    /**
     * RequestMapping
     */
    REQUEST_MAPPING("org.springframework.web.bind.annotation.RequestMapping", HttpMethod.REQUEST),

    /**
     * GetMapping
     */
    GET_MAPPING("org.springframework.web.bind.annotation.GetMapping", HttpMethod.GET),

    /**
     * PostMapping
     */
    POST_MAPPING("org.springframework.web.bind.annotation.PostMapping", HttpMethod.POST),

    /**
     * PutMapping
     */
    PUT_MAPPING("org.springframework.web.bind.annotation.PutMapping", HttpMethod.PUT),

    /**
     * DeleteMapping
     */
    DELETE_MAPPING("org.springframework.web.bind.annotation.DeleteMapping", HttpMethod.DELETE),

    /**
     * PatchMapping
     */
    PATCH_MAPPING("org.springframework.web.bind.annotation.PatchMapping", HttpMethod.PATCH),

    /**
     * RequestParam
     */
    REQUEST_PARAM("org.springframework.web.bind.annotation.RequestParam", null),

    /**
     * RequestBody
     */
    REQUEST_BODY("org.springframework.web.bind.annotation.RequestBody", null),

    /**
     * PathVariable
     */
    PATH_VARIABLE("org.springframework.web.bind.annotation.PathVariable", null),

    /**
     * RequestHeader
     */
    REQUEST_HEADER("org.springframework.web.bind.annotation.RequestHeader", null);

    private final String qualifiedName;
    private final HttpMethod method;

    SpringHttpMethodAnnotation(String qualifiedName, HttpMethod method) {
        this.qualifiedName = qualifiedName;
        this.method = method;
    }

    @Nullable
    public static SpringHttpMethodAnnotation getByQualifiedName(String qualifiedName) {
        for (SpringHttpMethodAnnotation springRequestAnnotation : SpringHttpMethodAnnotation.values()) {
            if (springRequestAnnotation.getQualifiedName().equals(qualifiedName)) {
                return springRequestAnnotation;
            }
        }
        return null;
    }

    @Nullable
    public static SpringHttpMethodAnnotation getByShortName(String requestMapping) {
        for (SpringHttpMethodAnnotation springRequestAnnotation : SpringHttpMethodAnnotation.values()) {
            if (springRequestAnnotation.getQualifiedName().endsWith(requestMapping)) {
                return springRequestAnnotation;
            }
        }
        return null;
    }

    public HttpMethod getMethod() {
        return this.method;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    @NotNull
    public String getShortName() {
        return qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
    }
}