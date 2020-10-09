/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: PomUtil
  Author:   ZhangYuanSheng
  Date:     2020/8/3 01:32
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class PomUtil {

    private static final Map<String, Document> POM_CACHES = PomCache.CACHES;

    private PomUtil() {
    }

    public static void clearCaches(Module... modules) {
        if (modules == null || modules.length < 1) {
            POM_CACHES.clear();
        } else {
            for (Module module : modules) {
                POM_CACHES.remove(ModuleUtil.getModuleDirPath(module));
            }
        }
    }

    @Nullable
    public static String getContextPathWithPom(@NotNull final Module module, @Nullable String contextPath) {
        if (contextPath == null) {
            return null;
        }
        @Language("RegExp") final String mavenPropReg = "@[a-zA-Z0-9.:-]+@";
        if (!RegUtil.contains(mavenPropReg, contextPath)) {
            return contextPath;
        }
        List<String> list = RegUtil.findAll(mavenPropReg, contextPath, 0);
        if (list == null || list.isEmpty()) {
            return contextPath;
        }
        Document pomDocument = getPomDocument(module);
        if (pomDocument == null) {
            return contextPath;
        }
        Element rootElement = pomDocument.getRootElement();
        for (String itemProp : list) {
            if (itemProp == null || itemProp.length() < 2) {
                continue;
            }
            final String itemPropName = "${" + itemProp.substring(1, itemProp.length() - 1) + "}";
            String replaceValue;
            replaceValue = getPomAttrOfProperties(rootElement.element("properties"), itemPropName);
            // 如果<properties>找不到则到根标签<project>寻找
            replaceValue = getPomAttrOfProject(rootElement, replaceValue);
            if (replaceValue != null && !replaceValue.equals(itemProp)) {
                contextPath = contextPath.replace(itemProp, replaceValue);
            }
        }
        return contextPath;
    }

    /**
     * 获取properties-element的值
     *
     * @param element element
     * @param name    name
     * @return value
     */
    @Nullable
    private static String getPomAttrOfProperties(@Nullable Element element, String name) {
        // maven element 的变量格式：${java.version}
        @Language("RegExp") final String propReg = "\\$\\{[A-Za-z0-9.:-]+}";
        if (name == null) {
            return null;
        }
        if (element == null) {
            return null;
        }
        if (!RegUtil.contains(propReg, name)) {
            return null;
        }
        String response = name;

        // 匹配的字段集合
        List<String> matchList = RegUtil.findAll(propReg, response, 0);
        Map<String, String> elements = new HashMap<>(matchList.size());
        for (String nameItem : matchList) {
            // 获取nameItem的对应节点
            Element itemElement = element.element(nameItem.substring(
                    nameItem.indexOf("{") + 1,
                    nameItem.indexOf("}")
            ));
            if (itemElement == null) {
                continue;
            }
            String elemData = itemElement.getData().toString().trim();
            if (RegUtil.contains(propReg, elemData)) {
                for (String itemName : RegUtil.findAll(propReg, elemData, 0)) {
                    String replacement = getPomAttrOfProperties(element, itemName);
                    if (replacement == null) {
                        continue;
                    }
                    elemData = elemData.replace(itemName, replacement);
                }
            }
            elements.put(nameItem, elemData);
        }
        for (Map.Entry<String, String> entry : elements.entrySet()) {
            String elemName = entry.getKey();
            String elemData = entry.getValue();
            response = response.replace(elemName, elemData);
        }
        return response;
    }

    /**
     * 获取project-element的值
     *
     * @param element element
     * @param name    name
     * @return value
     */
    @Nullable
    private static String getPomAttrOfProject(@Nullable Element element, String name) {
        // maven element 的变量格式：${project.version}
        @Language("RegExp") final String propReg = "\\$\\{[A-Za-z0-9.:-]+}";
        if (name == null) {
            return null;
        }
        if (element == null) {
            return null;
        }
        String response = name;
        for (String nameItem : RegUtil.findAll(propReg, response, 0)) {
            String elementName = nameItem.substring(
                    nameItem.indexOf("{") + 1,
                    nameItem.indexOf("}")
            );
            if (elementName.toLowerCase().startsWith("project.")) {
                elementName = elementName.substring(elementName.indexOf(".") + 1);
            }
            Element itemElement = element.element(elementName);
            if (itemElement != null) {
                String itemResult = itemElement.getData().toString().trim();
                for (String itemName : RegUtil.findAll(propReg, itemResult, 0)) {
                    String replacement = getPomAttrOfProject(element, itemName);
                    if (replacement == null) {
                        continue;
                    }
                    itemResult = itemResult.replace(itemName, replacement);
                }
                response = response.replace(nameItem, itemResult);
            }
        }
        return response;
    }

    /**
     * 获取当前module目录下的pom.xml文件的Document
     *
     * @param module module
     * @return Document
     */
    @Nullable
    private static Document getPomDocument(@NotNull Module module) {
        final String pomFileName = "pom.xml";
        final String moduleFilePath = ModuleUtil.getModuleDirPath(module);
        if (POM_CACHES.containsKey(moduleFilePath)) {
            return POM_CACHES.get(moduleFilePath);
        }
        try {
            File moduleFile = new File(moduleFilePath);
            if (!moduleFile.exists()) {
                throw new Exception();
            }
            File pomFile = new File(moduleFile, pomFileName);
            if (!pomFile.exists()) {
                throw new Exception();
            }
            SAXReader reader = new SAXReader();
            Document document = reader.read(pomFile);
            POM_CACHES.put(moduleFilePath, document);
            return document;
        } catch (Exception ignore) {
            return null;
        }
    }

    private static class PomCache {

        public static final Map<String, Document> CACHES = new HashMap<>();
    }
}
