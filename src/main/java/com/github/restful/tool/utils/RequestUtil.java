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

import com.github.restful.tool.beans.Request;
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
public class RequestUtil {

    private static final Map<NavigatablePsiElement, List<Request>> REQUEST_CACHE = Custom.REQUEST_CACHE;

    /**
     * 获取所有的Request
     *
     * @param project project
     * @return map-{key: moduleName, value: itemRequestList}
     */
    @NotNull
    public static Map<String, List<Request>> getAllRequests(@NotNull Project project) {
        return getAllRequests(project, false);
    }

    /**
     * 获取所有的Request
     *
     * @param hasEmpty 是否生成包含空Request的moduleName
     * @param project  project
     * @return map-{key: moduleName, value: itemRequestList}
     */
    @NotNull
    public static Map<String, List<Request>> getAllRequests(@NotNull Project project, boolean hasEmpty) {
        Map<String, List<Request>> map = new HashMap<>();
        REQUEST_CACHE.clear();

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            List<Request> requests = getModuleRequests(project, module);
            if (!hasEmpty && requests.isEmpty()) {
                continue;
            }
            map.put(module.getName(), requests);

            requests.forEach(request -> {
                List<Request> requestList;
                if (REQUEST_CACHE.containsKey(request.getPsiElement())) {
                    requestList = REQUEST_CACHE.get(request.getPsiElement());
                } else {
                    requestList = new ArrayList<>();
                    REQUEST_CACHE.put(request.getPsiElement(), requestList);
                }
                requestList.add(request);
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
    public static List<Request> getModuleRequests(@NotNull Project project, @NotNull Module module) {
        List<Request> requests = new ArrayList<>();

        // JAX-RS方式
        List<Request> jaxrsRequestByModule = JaxrsHelper.getJaxrsRequestByModule(project, module);
        if (!jaxrsRequestByModule.isEmpty()) {
            requests.addAll(jaxrsRequestByModule);
        }

        // Spring RESTFul方式
        List<Request> springRequestByModule = SpringHelper.getSpringRequestByModule(project, module);
        if (!springRequestByModule.isEmpty()) {
            requests.addAll(springRequestByModule);
        }
        return requests;
    }

    @NotNull
    public static List<Request> getRequestFromLoaded(@NotNull PsiMethod psiMethod) {
        if (!REQUEST_CACHE.containsKey(psiMethod)) {
            return Collections.emptyList();
        }
        List<Request> requests = REQUEST_CACHE.get(psiMethod);
        return requests == null ? Collections.emptyList() : requests;
    }

    private static class Custom {

        public static final Map<NavigatablePsiElement, List<Request>> REQUEST_CACHE = new HashMap<>();
    }
}
