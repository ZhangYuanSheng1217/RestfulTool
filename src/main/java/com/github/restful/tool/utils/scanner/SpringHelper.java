/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: SpringHelper
  Author:   ZhangYuanSheng
  Date:     2020/5/28 21:08
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils.scanner;

import com.github.restful.tool.annotation.SpringHttpMethodAnnotation;
import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.beans.Request;
import com.github.restful.tool.utils.ProjectConfigUtil;
import com.github.restful.tool.utils.RestUtil;
import com.github.restful.tool.utils.SystemUtil;
import com.intellij.lang.jvm.annotation.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class SpringHelper {

    @NotNull
    public static List<Request> getSpringRequestByModule(@NotNull Project project, @NotNull Module module) {
        List<Request> moduleList = new ArrayList<>(0);

        List<PsiClass> controllers = getAllControllerClass(project, module);
        if (controllers.isEmpty()) {
            return moduleList;
        }

        for (PsiClass controllerClass : controllers) {
            moduleList.addAll(getRequests(controllerClass));
        }

        moduleList.addAll(KotlinUtil.getKotlinRequests(project, module));

        return moduleList;
    }

    @NotNull
    public static List<Request> getRequests(@NotNull PsiClass psiClass) {
        List<Request> requests = new ArrayList<>();
        List<Request> parentRequests = new ArrayList<>();
        List<Request> childrenRequests = new ArrayList<>();

        PsiAnnotation psiAnnotation = RestUtil.getClassAnnotation(
                psiClass,
                SpringHttpMethodAnnotation.REQUEST_MAPPING.getQualifiedName(),
                SpringHttpMethodAnnotation.REQUEST_MAPPING.getShortName()
        );
        if (psiAnnotation != null) {
            parentRequests = getRequests(psiAnnotation, null);
        }

        PsiMethod[] psiMethods = psiClass.getAllMethods();
        for (PsiMethod psiMethod : psiMethods) {
            childrenRequests.addAll(getRequests(psiMethod));
        }
        if (parentRequests.isEmpty()) {
            requests.addAll(childrenRequests);
        } else {
            parentRequests.forEach(parentRequest -> childrenRequests.forEach(childrenRequest -> {
                Request request = childrenRequest.copyWithParent(parentRequest);
                requests.add(request);
            }));
        }
        return requests;
    }

    public static boolean hasRestful(@NotNull PsiClass psiClass) {
        return psiClass.hasAnnotation(Control.Controller.getQualifiedName()) || psiClass.hasAnnotation(Control.RestController.getQualifiedName());
    }

    /**
     * 获取所有的控制器类
     *
     * @param project project
     * @param module  module
     * @return Collection<PsiClass>
     */
    @NotNull
    private static List<PsiClass> getAllControllerClass(@NotNull Project project, @NotNull Module module) {
        List<PsiClass> allControllerClass = new ArrayList<>();

        GlobalSearchScope moduleScope = ProjectConfigUtil.getModuleScope(module);
        Collection<PsiAnnotation> pathList = JavaAnnotationIndex.getInstance().get(
                Control.Controller.getName(),
                project,
                moduleScope
        );
        pathList.addAll(JavaAnnotationIndex.getInstance().get(
                Control.RestController.getName(),
                project,
                moduleScope
        ));
        for (PsiAnnotation psiAnnotation : pathList) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();

            if (!(psiElement instanceof PsiClass)) {
                continue;
            }

            PsiClass psiClass = (PsiClass) psiElement;
            allControllerClass.add(psiClass);
        }
        return allControllerClass;
    }

    /**
     * 获取注解中的参数，生成RequestBean
     *
     * @param annotation annotation
     * @return list
     * @see SpringHelper#getRequests(PsiMethod)
     */
    @NotNull
    private static List<Request> getRequests(@NotNull PsiAnnotation annotation, @Nullable PsiMethod psiMethod) {
        SpringHttpMethodAnnotation spring = SpringHttpMethodAnnotation.getByQualifiedName(
                annotation.getQualifiedName()
        );
        if (annotation.getResolveScope().isSearchInLibraries()) {
            spring = SpringHttpMethodAnnotation.getByShortName(annotation.getQualifiedName());
        }
        Set<HttpMethod> methods = new HashSet<>();
        List<String> paths = new ArrayList<>();
        CustomRefAnnotation refAnnotation = null;
        if (spring == null) {
            refAnnotation = findCustomAnnotation(annotation);
            if (refAnnotation == null) {
                return Collections.emptyList();
            }
            methods.addAll(refAnnotation.getMethods());
        } else {
            methods.add(spring.getMethod());
        }

        // 是否为隐式的path（未定义value或者path）
        boolean hasImplicitPath = true;
        List<JvmAnnotationAttribute> attributes = annotation.getAttributes();
        for (JvmAnnotationAttribute attribute : attributes) {
            String name = attribute.getAttributeName();

            if (methods.contains(HttpMethod.REQUEST) && "method".equals(name)) {
                // method可能为数组
                Object value = RestUtil.getAttributeValue(attribute.getAttributeValue());
                if (value instanceof String) {
                    methods.add(HttpMethod.parse(value));
                } else if (value instanceof List) {
                    //noinspection unchecked,rawtypes
                    List<String> list = (List) value;
                    for (String item : list) {
                        if (item != null) {
                            item = item.substring(item.lastIndexOf(".") + 1);
                            methods.add(HttpMethod.parse(item));
                        }
                    }
                }
            }

            boolean flag = false;
            for (String path : new String[]{"value", "path"}) {
                if (path.equals(name)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                continue;
            }
            Object value = RestUtil.getAttributeValue(attribute.getAttributeValue());
            if (value instanceof String) {
                paths.add(SystemUtil.formatPath(value));
            } else if (value instanceof List) {
                //noinspection unchecked,rawtypes
                List<Object> list = (List) value;
                list.forEach(item -> paths.add(SystemUtil.formatPath(item)));
            } else {
                throw new RuntimeException(String.format(
                        "Scan api: %s\n" +
                                "Class: %s",
                        value,
                        value != null ? value.getClass() : null
                ));
            }
            hasImplicitPath = false;
        }
        if (hasImplicitPath) {
            if (psiMethod != null) {
                List<String> loopPaths;
                if (refAnnotation != null && !(loopPaths = refAnnotation.getPaths()).isEmpty()) {
                    paths.addAll(loopPaths);
                } else {
                    paths.add("/");
                }
            }
        }

        List<Request> requests = new ArrayList<>(paths.size());

        paths.forEach(path -> {
            for (HttpMethod method : methods) {
                if (method.equals(HttpMethod.REQUEST) && methods.size() > 1) {
                    continue;
                }
                requests.add(new Request(
                        method,
                        path,
                        psiMethod
                ));
            }
        });
        return requests;
    }

    /**
     * 获取方法中的参数请求，生成RequestBean
     *
     * @param method Psi方法
     * @return list
     */
    @NotNull
    private static List<Request> getRequests(@NotNull PsiMethod method) {
        List<Request> requests = new ArrayList<>();
        for (PsiAnnotation annotation : RestUtil.getMethodAnnotations(method)) {
            requests.addAll(getRequests(annotation, method));
        }

        return requests;
    }

    @Nullable
    private static CustomRefAnnotation findCustomAnnotation(@NotNull PsiAnnotation psiAnnotation) {
        PsiAnnotation qualifiedAnnotation = RestUtil.getQualifiedAnnotation(
                psiAnnotation,
                SpringHttpMethodAnnotation.REQUEST_MAPPING.getQualifiedName()
        );
        if (qualifiedAnnotation == null) {
            return null;
        }
        CustomRefAnnotation otherAnnotation = new CustomRefAnnotation();

        for (JvmAnnotationAttribute attribute : qualifiedAnnotation.getAttributes()) {
            Object methodValues = getAnnotationValue(attribute, "method");
            if (methodValues != null) {
                List<?> methods = methodValues instanceof List ? ((List<?>) methodValues) : Collections.singletonList(methodValues);
                if (methods.isEmpty()) {
                    continue;
                }
                for (Object method : methods) {
                    if (method == null) {
                        continue;
                    }
                    otherAnnotation.addMethods(HttpMethod.parse(method));
                }
                continue;
            }

            Object pathValues = getAnnotationValue(attribute, "path", "value");
            if (pathValues != null) {
                List<?> paths = pathValues instanceof List ? ((List<?>) pathValues) : Collections.singletonList(pathValues);
                if (!paths.isEmpty()) {
                    for (Object path : paths) {
                        if (path == null) {
                            continue;
                        }
                        otherAnnotation.addPath((String) path);
                    }
                }
            }
        }
        return otherAnnotation;
    }

    @Nullable
    private static Object getAnnotationValue(@NotNull JvmAnnotationAttribute attribute, @NotNull String... attrNames) {
        String attributeName = attribute.getAttributeName();
        if (attrNames.length < 1) {
            return null;
        }
        boolean matchAttrName = false;
        for (String attrName : attrNames) {
            if (attributeName.equals(attrName)) {
                matchAttrName = true;
                break;
            }
        }
        if (!matchAttrName) {
            return null;
        }
        JvmAnnotationAttributeValue attributeValue = attribute.getAttributeValue();
        return getAttributeValue(attributeValue);
    }

    private static Object getAttributeValue(@Nullable JvmAnnotationAttributeValue attributeValue) {
        if (attributeValue == null) {
            return null;
        }
        if (attributeValue instanceof JvmAnnotationConstantValue) {
            Object constantValue = ((JvmAnnotationConstantValue) attributeValue).getConstantValue();
            return constantValue == null ? null : constantValue.toString();
        } else if (attributeValue instanceof JvmAnnotationEnumFieldValue) {
            return ((JvmAnnotationEnumFieldValue) attributeValue).getFieldName();
        } else if (attributeValue instanceof JvmAnnotationArrayValue) {
            List<String> values = new ArrayList<>();
            for (JvmAnnotationAttributeValue value : ((JvmAnnotationArrayValue) attributeValue).getValues()) {
                values.add((String) getAttributeValue(value));
            }
            return values;
        }
        return null;
    }

    /**
     * 是否是Restful的项目
     *
     * @param project project
     * @param module  module
     * @return bool
     */
    public static boolean isRestfulProject(@NotNull final Project project, @NotNull final Module module) {
        try {
            JavaAnnotationIndex instance = JavaAnnotationIndex.getInstance();
            Set<PsiAnnotation> annotations = new HashSet<>(instance.get(Control.Controller.getName(), project, module.getModuleScope()));
            if (!annotations.isEmpty()) {
                for (PsiAnnotation annotation : annotations) {
                    if (annotation == null) {
                        continue;
                    }
                    if (Control.Controller.getQualifiedName().equals(annotation.getQualifiedName())) {
                        return true;
                    }
                }
            }
            annotations.clear();
            annotations.addAll(instance.get(Control.RestController.getName(), project, module.getModuleScope()));
            if (!annotations.isEmpty()) {
                for (PsiAnnotation annotation : annotations) {
                    if (annotation == null) {
                        continue;
                    }
                    if (Control.RestController.getQualifiedName().equals(annotation.getQualifiedName())) {
                        return true;
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    enum Control {

        /**
         * <p>@Controller</p>
         */
        Controller("Controller", "org.springframework.stereotype.Controller"),

        /**
         * <p>@RestController</p>
         */
        RestController("RestController", "org.springframework.web.bind.annotation.RestController");

        private final String name;
        private final String qualifiedName;

        Control(String name, String qualifiedName) {
            this.name = name;
            this.qualifiedName = qualifiedName;
        }

        public String getName() {
            return name;
        }

        public String getQualifiedName() {
            return qualifiedName;
        }
    }

    private static class CustomRefAnnotation {

        private final List<String> paths;
        private final List<HttpMethod> methods;

        public CustomRefAnnotation() {
            this.paths = new ArrayList<>();
            this.methods = new ArrayList<>();
        }

        public void addPath(@NotNull String... paths) {
            if (paths.length < 1) {
                return;
            }
            this.paths.addAll(Arrays.asList(paths));
        }

        public void addMethods(@NotNull HttpMethod... methods) {
            if (methods.length < 1) {
                return;
            }
            this.methods.addAll(Arrays.asList(methods));
        }

        public List<String> getPaths() {
            return paths;
        }

        public List<HttpMethod> getMethods() {
            return methods;
        }
    }
}
