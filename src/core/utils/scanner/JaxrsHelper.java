/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: JaxrsHelper
  Author:   ZhangYuanSheng
  Date:     2020/5/28 21:01
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package core.utils.scanner;

import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.xml.XmlFile;
import core.annotation.JaxrsHttpMethodAnnotation;
import core.beans.HttpMethod;
import core.beans.Request;
import core.utils.RestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class JaxrsHelper {

    @NotNull
    public static List<Request> getJaxrsRequestByModule(@NotNull Project project, @NotNull Module module) {
        List<Request> requests = new ArrayList<>();
        for (PsiClass psiClass : scanHasPathFiles(project, module)) {
            requests.addAll(getRequestsFromClass(getRootPathOfClass(psiClass), psiClass));
        }
        return requests;
    }

    @NotNull
    private static List<PsiClass> scanHasPathFiles(@NotNull Project project, @NotNull Module module) {
        List<PsiClass> classList = new ArrayList<>();

        XmlFile webXml = findWebConfigFile(project, module);
        if (webXml == null) {
            Collection<PsiAnnotation> pathList = JavaAnnotationIndex.getInstance().get(
                    Control.Path.getName(),
                    project,
                    RestUtil.getModuleScope(module)
            );
            for (PsiAnnotation psiAnnotation : pathList) {
                PsiElement psiElement = psiAnnotation.getParent().getParent();

                if (!(psiElement instanceof PsiClass)) {
                    continue;
                }

                PsiClass psiClass = (PsiClass) psiElement;
                classList.add(psiClass);
            }
        } else {
            classList.addAll(parseWebXml(project, module));
        }

        return classList;
    }

    @NotNull
    private static List<Request> getRequestsFromClass(@Nullable String rootPath, @NotNull PsiClass psiClass) {
        List<Request> childrenRequests = new ArrayList<>();

        PsiMethod[] psiMethods = psiClass.getMethods();
        for (PsiMethod psiMethod : psiMethods) {
            String path = "/";
            List<HttpMethod> methods = new ArrayList<>();

            PsiAnnotation[] annotations = psiMethod.getModifierList().getAnnotations();
            for (PsiAnnotation annotation : annotations) {
                Control controlPath = Control.getPathByQualifiedName(annotation.getQualifiedName());
                if (controlPath != null) {
                    List<JvmAnnotationAttribute> attributes = annotation.getAttributes();
                    Object value = RestUtil.getAttributeValue(attributes.get(0).getAttributeValue());
                    if (value != null) {
                        path = (String) value;
                    }
                }

                JaxrsHttpMethodAnnotation jaxrs = JaxrsHttpMethodAnnotation.getByQualifiedName(
                        annotation.getQualifiedName()
                );
                if (jaxrs != null) {
                    methods.add(jaxrs.getMethod());
                }
            }

            for (HttpMethod method : methods) {
                String tempPath = path;
                if (!tempPath.startsWith("/")) {
                    tempPath = "/" + tempPath;
                }
                if (rootPath != null) {
                    tempPath = rootPath + tempPath;
                    if (!tempPath.startsWith("/")) {
                        tempPath = "/" + tempPath;
                    }
                    tempPath = tempPath.replaceAll("//", "/");
                }
                Request request = new Request(method, tempPath, psiMethod);
                childrenRequests.add(request);
            }
        }
        return childrenRequests;
    }

    @Nullable
    private static String getRootPathOfClass(@NotNull PsiClass psiClass) {
        PsiAnnotation psiAnnotation = psiClass.getAnnotation(
                Control.Path.getQualifiedName()
        );
        if (psiAnnotation != null) {
            return (String) RestUtil.getAttributeValue(psiAnnotation.getAttributes().get(0).getAttributeValue());
        }
        return null;
    }

    /**
     * 查找web.xml配置文件
     *
     * @param project project
     * @param module  module
     * @return xmlFile
     */
    @Nullable
    private static XmlFile findWebConfigFile(@NotNull Project project, @NotNull Module module) {
        PsiFile[] files = FilenameIndex.getFilesByName(project, "web.xml", module.getModuleScope());
        for (PsiFile file : files) {
            System.out.println("file = " + file.getName());
        }
        for (PsiFile file : files) {
            if (file instanceof XmlFile) {
                return (XmlFile) file;
            }
        }
        return null;
    }

    @NotNull
    private static List<PsiClass> parseWebXml(@NotNull Project project, @NotNull Module module) {
        // TODO parse Xml
        return Collections.emptyList();
    }

    enum Control {

        /**
         * Javax.ws.rs.Path
         */
        Path("Path", "javax.ws.rs.Path");

        private final String name;
        private final String qualifiedName;

        Control(String name, String qualifiedName) {
            this.name = name;
            this.qualifiedName = qualifiedName;
        }

        @Nullable
        public static Control getPathByQualifiedName(String qualifiedName) {
            for (Control annotation : Control.values()) {
                if (annotation.getQualifiedName().equals(qualifiedName)) {
                    return annotation;
                }
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public String getQualifiedName() {
            return qualifiedName;
        }
    }
}
