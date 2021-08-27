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
import com.github.restful.tool.utils.data.ModuleConfigs;
import com.github.restful.tool.utils.data.ModuleHeaders;
import com.github.restful.tool.utils.scanner.IFrameworkHelper;
import com.github.restful.tool.utils.scanner.IJavaFramework;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.openapi.module.ModuleUtil.findModuleForFile;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ApiServices {

    private ApiServices() {
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

        for (Module module : modules) {
            List<ApiService> apiServices = getModuleApis(project, module);
            if (!hasEmpty && apiServices.isEmpty()) {
                continue;
            }
            map.put(module.getName(), apiServices);
        }
        return map;
    }

    private static List<ApiService> fill(@NotNull Project project, @NotNull String moduleName,
                                         @NotNull List<ApiService> apiServices) {
        // 填充模块url前缀
        Map<String, String> moduleConfig = ModuleConfigs.getModuleConfig(project, moduleName);

        // 填充HttpHeader
        Map<String, String> moduleHeader = ModuleHeaders.getModuleHeader(project, moduleName);

        apiServices.forEach(api -> {
            if (!moduleConfig.isEmpty()) {
                ModuleConfigs.Config.apply(moduleConfig, api);
            }
            if (!moduleHeader.isEmpty()) {
                ModuleHeaders.apply(moduleHeader, api);
            }
        });
        return apiServices;
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

        for (IJavaFramework helper : IFrameworkHelper.getJavaHelpers()) {
            Collection<ApiService> service = helper.getService(project, module);
            if (service.isEmpty()) {
                continue;
            }
            apiServices.addAll(service);
        }

        return fill(project, module.getName(), apiServices);
    }

    @NotNull
    public static List<ApiService> getCurrClassRequests(@Nullable PsiClass psiClass) {
        if (psiClass == null) {
            return Collections.emptyList();
        }

        List<ApiService> apiServices = new ArrayList<>();

        for (IJavaFramework helper : IFrameworkHelper.getJavaHelpers()) {
            Collection<ApiService> service = helper.getService(psiClass);
            if (service.isEmpty()) {
                continue;
            }
            apiServices.addAll(service);
        }

        Project project = psiClass.getProject();
        Module module = findModuleForFile(psiClass.getContainingFile());
        if (module == null) {
            return apiServices;
        }
        return fill(project, module.getName(), apiServices);
    }

    /**
     * 是否是Restful的项目
     *
     * @param project project
     * @return bool
     */
    public static boolean isRestfulProject(@NotNull Project project) {
        ModuleManager manager = ModuleManager.getInstance(project);
        for (Module module : manager.getModules()) {
            for (IJavaFramework helper : IFrameworkHelper.getJavaHelpers()) {
                if (helper.isRestfulProject(project, module)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检测当前 PsiClass 是否含有`RestController` | `Controller` | `Path`
     *
     * @param psiClass psiClass
     * @return bool
     */
    public static boolean hasRestful(@Nullable PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        for (IJavaFramework helper : IFrameworkHelper.getJavaHelpers()) {
            if (helper.hasRestful(psiClass)) {
                return true;
            }
        }
        return false;
    }
}
