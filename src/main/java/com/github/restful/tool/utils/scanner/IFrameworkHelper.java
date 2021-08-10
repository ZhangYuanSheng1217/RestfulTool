package com.github.restful.tool.utils.scanner;

import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.utils.scanner.framework.Jaxrs;
import com.github.restful.tool.utils.scanner.framework.Rose;
import com.github.restful.tool.utils.scanner.framework.Spring;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 框架扫描帮助接口
 * <p>
 * C: PsiClass | KtClass
 *
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface IFrameworkHelper<C> {

    @NotNull
    static List<IJavaFramework> getJavaHelpers() {
        return Arrays.asList(
                Spring.getInstance(),
                Jaxrs.getInstance(),
                Rose.getInstance()
        );
    }

    /**
     * 是否是Restful的项目
     *
     * @param project project
     * @param module  module
     * @return bool
     */
    boolean isRestfulProject(@NotNull Project project, @NotNull Module module);

    /**
     * 根据模块获取所有的Services
     *
     * @param project 项目
     * @param module  模块
     * @return Collection
     */
    Collection<ApiService> getService(@NotNull Project project, @NotNull Module module);

    /**
     * 根据文件获取所有的Services
     *
     * @param clazz class文件
     * @return Collection
     */
    Collection<ApiService> getService(@NotNull C clazz);

    /**
     * 检测当前 PsiClass 是否含有`RestController` | `Controller` | `Path`
     *
     * @param clazz class文件
     * @return bool
     */
    boolean hasRestful(@Nullable C clazz);
}
