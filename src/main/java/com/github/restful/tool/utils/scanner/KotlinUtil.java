/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: KotlinUtil
  Author:   ZhangYuanSheng
  Date:     2020/9/6 22:35
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils.scanner;

import com.github.restful.tool.annotation.SpringHttpMethodAnnotation;
import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.beans.PropertiesKey;
import com.github.restful.tool.beans.Request;
import com.github.restful.tool.beans.ServiceStub;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.stubindex.KotlinAnnotationsIndex;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.*;

import java.util.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class KotlinUtil {

    public final Project PROJECT;
    public final Module MODULE;

    private KotlinUtil(@NotNull Module module) {
        PROJECT = module.getProject();
        this.MODULE = module;
    }

    public static KotlinUtil create(@NotNull Module module) {
        return new KotlinUtil(module);
    }

    @NotNull
    public static List<Request> getKotlinRequests(@NotNull Project project, @NotNull Module module) {
        List<Request> ktRequests = new ArrayList<>();
        KotlinUtil kotlinUtil = create(module);
        List<KtClass> kotlinClasses = kotlinUtil.getRestfulKotlinClasses(PropertiesKey.scanServiceWithLibrary(project));
        for (KtClass kotlinClass : kotlinClasses) {
            ServiceStub clsStub = null;
            for (KtAnnotationEntry annotationEntry : kotlinClass.getAnnotationEntries()) {
                if ((clsStub = getServiceStub(annotationEntry)) != null) {
                    break;
                }
            }

            List<ServiceStub> stubs = new ArrayList<>();
            for (KtNamedFunction function : kotlinUtil.getFunctionsOnKtClass(kotlinClass)) {
                for (KtAnnotationEntry annotationEntry : function.getAnnotationEntries()) {
                    ServiceStub serviceStub = getServiceStub(annotationEntry);
                    if (serviceStub == null) {
                        continue;
                    }
                    serviceStub.setPsiElement(function);
                    stubs.add(serviceStub);
                }
            }

            List<String> parentPaths = new ArrayList<>();
            List<HttpMethod> parentMethods = new ArrayList<>();
            List<Request> children = new ArrayList<>();
            if (clsStub != null) {
                parentPaths.addAll(clsStub.getPaths());
                parentMethods.addAll(clsStub.getMethods());
            }
            for (ServiceStub stub : stubs) {
                for (HttpMethod method : stub.getMethods()) {
                    for (String path : stub.getPaths()) {
                        children.add(new Request(method, path, stub.getPsiElement()));
                    }
                }
            }

            if (parentPaths.isEmpty()) {
                ktRequests.addAll(children);
            } else {
                parentPaths.forEach(parentPath -> children.forEach(childrenRequest -> {
                    if (childrenRequest.getMethod() != null && childrenRequest.getMethod() != HttpMethod.REQUEST) {
                        Request request = childrenRequest.copyWithParent(
                                new Request(null, parentPath, null)
                        );
                        ktRequests.add(request);
                    } else {
                        for (HttpMethod parentMethod : parentMethods) {
                            Request request = childrenRequest.copyWithParent(
                                    new Request(parentMethod, parentPath, null)
                            );
                            ktRequests.add(request);
                        }
                    }
                }));
            }
        }
        return ktRequests;
    }

    /**
     * 根据Kotlin的注解获取ServiceStub
     *
     * @param ktAnnotationEntry Kotlin注解
     * @return ServiceStub
     * @see ServiceStub
     */
    @Nullable
    public static ServiceStub getServiceStub(@NotNull KtAnnotationEntry ktAnnotationEntry) {
        Name shortName = ktAnnotationEntry.getShortName();
        if (shortName == null) {
            return null;
        }
        String annotationName = shortName.asString();
        if (annotationName.contains(".")) {
            // 去掉 package
            annotationName = annotationName.substring(annotationName.lastIndexOf(".") + 1);
        }
        SpringHttpMethodAnnotation springHttpMethodAnnotation;
        if ((springHttpMethodAnnotation = SpringHttpMethodAnnotation.getByShortName(annotationName)) == null) {
            return null;
        }

        ServiceStub serviceStub = new ServiceStub(ktAnnotationEntry);
        serviceStub.method(springHttpMethodAnnotation.getMethod());

        List<? extends ValueArgument> valueArguments = ktAnnotationEntry.getValueArguments();
        if (valueArguments.isEmpty()) {
            // 未设置注解的 value：@GetMapping
            serviceStub.path(null);
        } else {
            for (ValueArgument valueArgument : valueArguments) {
                if (!(valueArgument instanceof KtValueArgument)) {
                    continue;
                }
                KtValueArgument ktValueArgument = (KtValueArgument) valueArgument;
                if (ktValueArgument.isNamed()) {
                    KtValueArgumentName argumentName = ktValueArgument.getArgumentName();
                    if (argumentName == null) {
                        continue;
                    }
                    String name = argumentName.getAsName().asString();
                    if ("method".equals(name)) {
                        KtExpression argumentExpression = ktValueArgument.getArgumentExpression();
                        if (argumentExpression instanceof KtCollectionLiteralExpression) {
                            KtCollectionLiteralExpression expression = (KtCollectionLiteralExpression) argumentExpression;
                            for (PsiElement child : expression.getChildren()) {
                                if (!(child instanceof KtDotQualifiedExpression)) {
                                    continue;
                                }
                                KtDotQualifiedExpression qualifiedExpression = (KtDotQualifiedExpression) child;
                                if (qualifiedExpression.getChildren().length != 2) {
                                    continue;
                                }
                                if (!(qualifiedExpression.getChildren()[0] instanceof KtNameReferenceExpression)
                                        || !(qualifiedExpression.getChildren()[1] instanceof KtNameReferenceExpression)) {
                                    continue;
                                }
                                KtNameReferenceExpression referenceExpression = (KtNameReferenceExpression) qualifiedExpression.getChildren()[1];
                                // 获取到当前注解的 HttpMethods 的其中一个(GET|POST|...)
                                final String currHttpMethod = referenceExpression.getText();
                                serviceStub.method(HttpMethod.parse(currHttpMethod));
                            }
                        }
                    }
                    if (!("path".equals(name) || "value".equals(name))) {
                        continue;
                    }
                }
                KtExpression expression = ktValueArgument.getArgumentExpression();
                if (expression instanceof KtStringTemplateExpression) {
                    KtStringTemplateExpression stringTemplateExpression = (KtStringTemplateExpression) expression;
                    final String currPath = stringTemplateExpression.getChildren()[0].getText();
                    // 当前 paths 的其中一个（@RequestMapping(path=["path1", "path2"])）
                    serviceStub.path(currPath);
                    continue;
                }
                if (!(expression instanceof KtCollectionLiteralExpression)) {
                    continue;
                }
                KtCollectionLiteralExpression collectionLiteralExpression = (KtCollectionLiteralExpression) expression;
                if (collectionLiteralExpression.getChildren().length == 0) {
                    // 未设置注解的 value：@GetMapping
                    serviceStub.path(null);
                } else {
                    for (PsiElement psiElement : collectionLiteralExpression.getChildren()) {
                        if (!(psiElement instanceof KtStringTemplateExpression)) {
                            continue;
                        }
                        KtStringTemplateExpression stringTemplateExpression = (KtStringTemplateExpression) psiElement;
                        final String currPath = stringTemplateExpression.getChildren()[0].getText();
                        // 当前 paths 的其中一个（@RequestMapping(path=["path1", "path2"])）
                        serviceStub.path(currPath);
                    }
                }
            }
        }

        return serviceStub;
    }

    public List<KtClass> getRestfulKotlinClasses(boolean withLib) {
        List<KtClass> classes = new ArrayList<>();

        for (SpringHelper.Control control : SpringHelper.Control.values()) {
            String name = withLib ? control.getQualifiedName() : control.getName();
            List<KtClass> list = findKtClassByAnnotationName(name, withLib);
            if (list.isEmpty()) {
                continue;
            }
            classes.addAll(list);
        }

        return classes;
    }

    public List<KtNamedFunction> getFunctionsOnKtClass(@NotNull KtClass ktClass) {
        List<KtNamedFunction> functions = new ArrayList<>();
        for (KtDeclaration declaration : ktClass.getDeclarations()) {
            if (!(declaration instanceof KtNamedFunction)) {
                continue;
            }
            functions.add(((KtNamedFunction) declaration));
        }
        return functions;
    }

    @NotNull
    public List<KtClass> findKtClassByAnnotationName(@NotNull String name, boolean withLib) {
        List<KtClass> classes = new ArrayList<>();
        for (KtAnnotationEntry entry : findKtAnnotationEntryByName(name, withLib)) {
            PsiElement context = entry.getContext();
            if (context == null) {
                continue;
            }
            context = context.getContext();
            if (!(context instanceof KtClass)) {
                continue;
            }
            classes.add(((KtClass) context));
        }
        return classes;
    }

    @NotNull
    private Set<KtAnnotationEntry> findKtAnnotationEntryByName(@NotNull String name, boolean withLib) {
        String temp = name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : name;
        Set<KtAnnotationEntry> collection = new HashSet<>(KotlinAnnotationsIndex.getInstance().get(
                temp,
                PROJECT,
                MODULE.getModuleScope()
        ));
        if (withLib) {
            collection.addAll(KotlinAnnotationsIndex.getInstance().get(
                    name,
                    PROJECT,
                    MODULE.getModuleWithLibrariesScope())
            );
            collection.addAll(KotlinAnnotationsIndex.getInstance().get(
                    temp,
                    PROJECT,
                    MODULE.getModuleWithLibrariesScope())
            );
        }
        return collection;
    }
}
