package com.github.restful.tool.annotation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.restful.tool.beans.HttpMethod;

/**
 * @author wangkai
 */
public enum RoseHttpMethodAnnotation {

	/**
	 * Path
	 */
	PATH("net.paoding.rose.web.annotation.Path", HttpMethod.REQUEST),

	/**
	 * Get
	 */
	GET("net.paoding.rose.web.annotation.rest.Get", HttpMethod.GET),

	/**
	 * Post
	 */
	POST("net.paoding.rose.web.annotation.rest.Post", HttpMethod.POST),

	/**
	 * Put
	 */
	PUT("net.paoding.rose.web.annotation.rest.Put", HttpMethod.PUT),

	/**
	 * Delete
	 */
	DELETE("net.paoding.rose.web.annotation.rest.Delete", HttpMethod.DELETE),

	/**
	 * Param
	 */
	PARAM("net.paoding.rose.web.annotation.Param", null);

	private final String qualifiedName;
	private final HttpMethod method;

	RoseHttpMethodAnnotation(String qualifiedName, HttpMethod method) {
		this.qualifiedName = qualifiedName;
		this.method = method;
	}

	@Nullable
	public static RoseHttpMethodAnnotation getByQualifiedName(String qualifiedName) {
		for (RoseHttpMethodAnnotation roseHttpMethodAnnotation : RoseHttpMethodAnnotation.values()) {
			if (roseHttpMethodAnnotation.getQualifiedName().equals(qualifiedName)) {
				return roseHttpMethodAnnotation;
			}
		}
		return null;
	}

	@Nullable
	public static RoseHttpMethodAnnotation getByShortName(String requestMapping) {
		for (RoseHttpMethodAnnotation roseHttpMethodAnnotation : RoseHttpMethodAnnotation.values()) {
			if (roseHttpMethodAnnotation.getQualifiedName().endsWith(requestMapping)) {
				return roseHttpMethodAnnotation;
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
