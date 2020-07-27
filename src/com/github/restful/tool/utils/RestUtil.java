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

import cn.hutool.core.util.ReUtil;
import com.github.restful.tool.beans.Request;
import com.github.restful.tool.utils.scanner.JaxrsHelper;
import com.github.restful.tool.utils.scanner.SpringHelper;
import com.intellij.lang.jvm.annotation.JvmAnnotationArrayValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationClassValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationEnumFieldValue;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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
                    "server.port"
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
        // server.servlet.context-path
        try {
            String contextPath = ProjectConfigUtil.getApplicationConfig(
                    project, scope,
                    "server.servlet.context-path"
            );
            @Language("RegExp") final String mavenPropReg = "@\\S+@";
            @Nullable String mavenProp;
            if (contextPath != null && (mavenProp = ReUtil.getGroup0(mavenPropReg, contextPath)) != null) {
                Document pomDoc = getModulePomFile(((ModuleWithDependenciesScope) scope).getModule());
                if (pomDoc != null) {
                    mavenProp = replaceItem(pomDoc,
                            "${" + mavenProp.substring(
                                    mavenProp.indexOf("@") + 1, mavenProp.lastIndexOf("@")
                            ) + "}");
                }
                contextPath = ReUtil.replaceAll(contextPath, mavenPropReg, mavenProp);
            }
            return contextPath;
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

    /**
     * 从pom中替换变量
     *
     * @param pomDoc pom
     * @param substring 变量 ${xxx}
     */
    private static String replaceItem(Document pomDoc, String substring) {
        @Language("RegExp") final String propReg = "\\$\\{[A-Za-z0-9.:-]+}";
        for (String itemName : ReUtil.findAll(propReg, substring, 0)) {
            substring = getPomFileProject(pomDoc, itemName);
            if (substring.startsWith("${")) {
                substring = getPomFileProperties(pomDoc, itemName);
            }
        }
        return substring;
    }

    /**
     * 获取properties-element的值
     *
     * @param pomDoc pomDoc
     * @param name    name
     * @return value
     */
    @NotNull
    private static String getPomFileProperties(@Nullable Document pomDoc, String name) {
        // maven element 的变量格式：${java.version}
        @Language("RegExp") final String propReg = "\\$\\{[A-Za-z0-9.:-]+}";
        if (name == null) {
            return "";
        }
        Element element = pomDoc.getRootElement().element("properties");
        if (element == null) {
            return name;
        }
        Element itemElement = element.element(name.substring(
                name.indexOf("{") + 1,
                name.indexOf("}")
        ));
        if (itemElement != null) {
            String itemResult = itemElement.getData().toString().trim();
            for (String itemName : ReUtil.findAll(propReg, itemResult, 0)) {
                itemResult = itemResult.replace(itemName, replaceItem(pomDoc, itemName));
            }
            name = itemResult;
        }
        return name;
    }

    /**
     * 获取project-element的值
     *
     * @param pomDoc pomDoc
     * @param name    name
     * @return value
     */
    @NotNull
    private static String getPomFileProject(@Nullable Document pomDoc, String name) {
        // maven element 的变量格式：${project.version}
        @Language("RegExp") final String propReg = "\\$\\{[A-Za-z0-9.:-]+}";
        if (name == null) {
            return "";
        }
        Element element = pomDoc.getRootElement();
        if (element == null) {
            return name;
        }
        String elementName = name.substring(
                name.indexOf("{") + 1,
                name.indexOf("}")
        );
        Element currentElement = element;
        String[] path = elementName.split("\\.");
        for (int i = 1; i < path.length; i++) {
            currentElement = currentElement.element(path[i]);
            if (currentElement == null) {
                return name;
            }
        }
        String itemResult = currentElement.getData().toString().trim();
        for (String itemName : ReUtil.findAll(propReg, itemResult, 0)) {
            itemResult = itemResult.replace(itemName, replaceItem(pomDoc, itemName));
        }
        name = itemResult;
        return name;
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
    public static PsiAnnotation getClassAnnotation(@NotNull PsiClass psiClass, @NotNull String qualifiedName) {
        PsiAnnotation annotation = psiClass.getAnnotation(qualifiedName);
        if (annotation != null) {
            return annotation;
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

    /**
     * 通过绝对地址读取pom.xml文件并转换成Document
     *
     * @param module module
     * @return XmlFile
     */
    @Nullable
    private static Document getModulePomFile(@NotNull Module module) {
        String pomFileName = "pom.xml";
        try {
            File moduleFile = new File(module.getModuleFilePath());
            if (!moduleFile.exists()) {
                throw new Exception();
            }
            File pomFile = new File(moduleFile.getParent(), pomFileName);
            if (!pomFile.exists()) {
                throw new Exception();
            }
            SAXReader reader = new SAXReader();
            return reader.read(pomFile);
        } catch (Exception ignore) {
        }
        return null;
    }
}
