/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RequestUtil
  Author:   ZhangYuanSheng
  Date:     2020/7/16 16:14
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils;

import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.utils.scanner.JaxrsHelper;
import com.github.restful.tool.utils.scanner.SpringHelper;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ApiServiceUtil {

    private static final Map<NavigatablePsiElement, List<ApiService>> REQUEST_CACHE = Custom.REQUEST_CACHE;

    private ApiServiceUtil() {
        // private
    }

    /**
     * 获取所有的Request
     *
     * @param project project
     * @return map-{key: moduleName, value: itemRequestList}
     */
    @NotNull
    public static Map<String, List<ApiService>> getApis(@NotNull Project project) {
        return getApis(project, false);
    }

    /**
     * 获取所有的Request
     *
     * @param hasEmpty 是否生成包含空Request的moduleName
     * @param project  project
     * @return map-{key: moduleName, value: itemRequestList}
     */
    @NotNull
    public static Map<String, List<ApiService>> getApis(@NotNull Project project, boolean hasEmpty) {
        return getApis(project, ModuleManager.getInstance(project).getModules(), hasEmpty);
    }

    /**
     * 获取所有的Request
     *
     * @param hasEmpty 是否生成包含空Request的moduleName
     * @param modules  所有模块
     * @param project  project
     * @return map-{key: moduleName, value: itemRequestList}
     */
    @NotNull
    public static Map<String, List<ApiService>> getApis(@NotNull Project project, Module @NotNull [] modules, boolean hasEmpty) {
        Map<String, List<ApiService>> map = new HashMap<>();
        REQUEST_CACHE.clear();

        for (Module module : modules) {
            List<ApiService> apiServices = getModuleApis(project, module);
            if (!hasEmpty && apiServices.isEmpty()) {
                continue;
            }
            map.put(module.getName(), apiServices);

            apiServices.forEach(request -> {
                List<ApiService> apiServiceList;
                if (REQUEST_CACHE.containsKey(request.getPsiElement())) {
                    apiServiceList = REQUEST_CACHE.get(request.getPsiElement());
                } else {
                    apiServiceList = new ArrayList<>();
                    REQUEST_CACHE.put(request.getPsiElement(), apiServiceList);
                }
                apiServiceList.add(request);
            });
        }
        return map;
    }

    /**
     * 获取选中module的所有Request
     *
     * @param project project
     * @param module  module
     * @return list
     */
    @NotNull
    public static List<ApiService> getModuleApis(@NotNull Project project, @NotNull Module module) {
        List<ApiService> apiServices = new ArrayList<>();

        // JAX-RS方式
        List<ApiService> jaxrsApiServiceByModule = JaxrsHelper.getJaxrsRequestByModule(project, module);
        if (!jaxrsApiServiceByModule.isEmpty()) {
            apiServices.addAll(jaxrsApiServiceByModule);
        }

        // Spring RESTFul方式
        List<ApiService> springApiServiceByModule = SpringHelper.getSpringRequestByModule(project, module);
        if (!springApiServiceByModule.isEmpty()) {
            apiServices.addAll(springApiServiceByModule);
        }
        return apiServices;
    }

    @NotNull
    public static List<ApiService> getApisFromLoaded(@NotNull PsiMethod psiMethod) {
        if (!REQUEST_CACHE.containsKey(psiMethod)) {
            return Collections.emptyList();
        }
        List<ApiService> apiServices = REQUEST_CACHE.get(psiMethod);
        return apiServices == null ? Collections.emptyList() : apiServices;
    }

    private static class Custom {

        private static final Map<NavigatablePsiElement, List<ApiService>> REQUEST_CACHE = new HashMap<>();
    }
}
