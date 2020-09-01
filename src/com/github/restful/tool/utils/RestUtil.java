/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RestUtil
  Author:   ZhangYuanSheng
  Date:     2020/5/4 15:14
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils;

import com.github.restful.tool.beans.Request;
import com.github.restful.tool.utils.scanner.JaxrsHelper;
import com.github.restful.tool.utils.scanner.SpringHelper;
import com.intellij.lang.jvm.annotation.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestUtil {

    /**
     * 扫描服务端口
     *
     * @param project project
     * @param scope   scope
     * @return port
     */
    public static int scanListenerPort(@NotNull Project project, @NotNull GlobalSearchScope scope) {
        // listener of default server port
        int port = 8080;

        try {
            String value = ProjectConfigUtil.getApplicationConfig(
                    project, scope,
                    ProjectConfigUtil.SERVER_PORT
            );
            if (value == null || "".equals((value = value.trim()))) {
                throw new NumberFormatException();
            }
            port = Integer.parseInt(value);
        } catch (NumberFormatException ignore) {
        }
        return port;
    }

    /**
     * 扫描服务协议
     *
     * @param project project
     * @param scope   scope
     * @return protocol
     */
    @NotNull
    public static String scanListenerProtocol(@NotNull Project project, @NotNull GlobalSearchScope scope) {
        // default protocol
        String protocol = "http";

        try {
            String value = ProjectConfigUtil.getApplicationConfig(project, scope, "server.ssl.enabled");
            if (value == null || "".equals((value = value.trim()))) {
                throw new Exception();
            }
            if (Boolean.parseBoolean(value)) {
                protocol = "https";
            }
        } catch (Exception ignore) {
        }
        return protocol;
    }

    /**
     * 扫描请求路径前缀
     *
     * @param project project
     * @param scope   scope
     * @return path
     */
    @Nullable
    public static String scanContextPath(@NotNull Project project, @NotNull GlobalSearchScope scope) {
        try {
            String contextPath = ProjectConfigUtil.getApplicationConfig(
                    project, scope,
                    ProjectConfigUtil.SERVER_SERVLET_CONTEXT_PATH
            );
            return PomUtil.getContextPathWithPom(((ModuleWithDependenciesScope) scope).getModule(), contextPath);
        } catch (Exception ignore) {
            return null;
        }
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
        return SpringHelper.hasRestful(psiClass) || JaxrsHelper.hasRestful(psiClass);
    }

    @NotNull
    public static List<Request> getCurrClassRequests(@Nullable PsiClass psiClass) {
        if (psiClass == null) {
            return Collections.emptyList();
        }
        List<Request> requests;
        if (!(requests = SpringHelper.getRequests(psiClass)).isEmpty()) {
            return requests;
        }
        requests = JaxrsHelper.getCurrClassRequests(psiClass);
        return requests;
    }

    /**
     * 获取属性值
     *
     * @param attributeValue Psi属性
     * @return {Object | List}
     */
    @Nullable
    public static Object getAttributeValue(JvmAnnotationAttributeValue attributeValue) {
        if (attributeValue == null) {
            return null;
        }
        if (attributeValue instanceof JvmAnnotationConstantValue) {
            return ((JvmAnnotationConstantValue) attributeValue).getConstantValue();
        } else if (attributeValue instanceof JvmAnnotationEnumFieldValue) {
            return ((JvmAnnotationEnumFieldValue) attributeValue).getFieldName();
        } else if (attributeValue instanceof JvmAnnotationArrayValue) {
            List<JvmAnnotationAttributeValue> values = ((JvmAnnotationArrayValue) attributeValue).getValues();
            List<Object> list = new ArrayList<>(values.size());
            for (JvmAnnotationAttributeValue value : values) {
                Object o = getAttributeValue(value);
                if (o != null) {
                    list.add(o);
                } else {
                    // 如果是jar包里的JvmAnnotationConstantValue则无法正常获取值
                    try {
                        Class<? extends JvmAnnotationAttributeValue> clazz = value.getClass();
                        Field myElement = clazz.getSuperclass().getDeclaredField("myElement");
                        myElement.setAccessible(true);
                        Object elObj = myElement.get(value);
                        if (elObj instanceof PsiExpression) {
                            PsiExpression expression = (PsiExpression) elObj;
                            list.add(expression.getText());
                        }
                    } catch (Exception ignore) {
                    }
                }
            }
            return list;
        } else if (attributeValue instanceof JvmAnnotationClassValue) {
            return ((JvmAnnotationClassValue) attributeValue).getQualifiedName();
        }
        return null;
    }

    /**
     * 查找类上的指定注解（包括超类和接口）
     *
     * @param psiClass      PsiClass
     * @param qualifiedName 注解全限定名
     * @return annotation
     */
    @Nullable
    public static PsiAnnotation getClassAnnotation(@NotNull PsiClass psiClass, @NotNull String... qualifiedName) {
        if (qualifiedName.length < 1) {
            return null;
        }
        PsiAnnotation annotation;
        for (String name : qualifiedName) {
            annotation = psiClass.getAnnotation(name);
            if (annotation != null) {
                return annotation;
            }
        }
        List<PsiClass> classes = new ArrayList<>();
        classes.add(psiClass.getSuperClass());
        classes.addAll(Arrays.asList(psiClass.getInterfaces()));
        for (PsiClass superPsiClass : classes) {
            if (superPsiClass == null) {
                continue;
            }
            PsiAnnotation classAnnotation = getClassAnnotation(superPsiClass, qualifiedName);
            if (classAnnotation != null) {
                return classAnnotation;
            }
        }
        return null;
    }

    /**
     * 获取方法的所有注解（包括父类）
     *
     * @param psiMethod psiMethod
     * @return annotations
     */
    @NotNull
    public static List<PsiAnnotation> getMethodAnnotations(@NotNull PsiMethod psiMethod) {
        List<PsiAnnotation> annotations = new ArrayList<>(Arrays.asList(psiMethod.getModifierList().getAnnotations()));
        for (PsiMethod superMethod : psiMethod.findSuperMethods()) {
            getMethodAnnotations(superMethod)
                    .stream()
                    // 筛选：子类中方法定义了父类中方法存在的注解时只保留最上层的注解（即实现类的方法注解
                    .filter(annotation -> !annotations.contains(annotation))
                    .forEach(annotations::add);
        }
        return annotations;
    }

    @Nullable
    public static PsiAnnotation getQualifiedAnnotation(PsiAnnotation psiAnnotation, @NotNull String qualifiedName) {
        final String targetAnn = "java.lang.annotation.Target";
        final String documentedAnn = "java.lang.annotation.Documented";
        final String retentionAnn = "java.lang.annotation.Retention";
        if (psiAnnotation == null) {
            return null;
        }
        String annotationQualifiedName = psiAnnotation.getQualifiedName();
        if (qualifiedName.equals(annotationQualifiedName)) {
            return psiAnnotation;
        }
        if (targetAnn.equals(annotationQualifiedName) || documentedAnn.equals(annotationQualifiedName) || retentionAnn.equals(annotationQualifiedName)) {
            return null;
        }
        PsiJavaCodeReferenceElement element = psiAnnotation.getNameReferenceElement();
        if (element == null) {
            return null;
        }
        PsiElement resolve = element.resolve();
        if (!(resolve instanceof PsiClass)) {
            return null;
        }
        PsiClass psiClass = (PsiClass) resolve;
        if (!psiClass.isAnnotationType()) {
            return null;
        }
        PsiAnnotation annotation = psiClass.getAnnotation(qualifiedName);
        if (annotation != null && qualifiedName.equals(annotation.getQualifiedName())) {
            return annotation;
        }
        for (PsiAnnotation classAnnotation : psiClass.getAnnotations()) {
            PsiAnnotation qualifiedAnnotation = getQualifiedAnnotation(classAnnotation, qualifiedName);
            if (qualifiedAnnotation != null) {
                return qualifiedAnnotation;
            }
        }
        return null;
    }

    /**
     * 是否是Restful的项目
     *
     * @param project project
     * @return bool
     */
    public static boolean isRestfulProject(@NotNull Project project) {
        try {
            ModuleManager manager = ModuleManager.getInstance(project);
            for (Module module : manager.getModules()) {
                if (SpringHelper.isRestfulProject(project, module)
                        || JaxrsHelper.isRestfulProject(project, module)) {
                    return true;
                }
            }
        } catch (Exception ignore) {
        }
        return false;
    }
}
