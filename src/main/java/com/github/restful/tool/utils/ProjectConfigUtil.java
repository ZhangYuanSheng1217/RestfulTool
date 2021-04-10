/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: PropUtil
  Author:   ZhangYuanSheng
  Date:     2020/7/16 16:05
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils;

import com.github.restful.tool.beans.PropertiesKey;
import com.github.restful.tool.beans.settings.Settings;
import com.github.restful.tool.service.Notify;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.Arrays;
import java.util.List;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ProjectConfigUtil {

    /**
     * 端口
     */
    public static final String SERVER_PORT = "server.port";
    /**
     * 上下文路径
     */
    public static final String SERVER_SERVLET_CONTEXT_PATH = "server.servlet.context-path";

    /**
     * 获取bootstrap配置的kv值
     *
     * @param project  project
     * @param scope    scope
     * @param propName name
     * @return {value | null}
     */
    @Nullable
    public static String getBootstrapConfig(@NotNull Project project,
                                            @NotNull GlobalSearchScope scope,
                                            @NotNull String propName) {
        final String bootstrapName = "bootstrap";
        final String[] bootstrapNames = {
                bootstrapName + "." + YAMLFileType.DEFAULT_EXTENSION,
                bootstrapName + "." + PropertiesFileType.DEFAULT_EXTENSION,
        };

        PsiFile psiFile = null;
        for (String name : bootstrapNames) {
            psiFile = getConfigurationPsiFile(project, scope, name);
            if (psiFile != null) {
                break;
            }
        }

        if (psiFile == null) {
            return null;
        }
        return getConfigValue(psiFile, propName);
    }

    /**
     * 获取application配置的kv值
     *
     * @param project  project
     * @param scope    scope
     * @param propName propName
     * @return {value | null}
     */
    @Nullable
    public static String getApplicationConfig(@NotNull Project project,
                                              @NotNull GlobalSearchScope scope,
                                              @NotNull String propName) {
        String bootstrapConfigValue = getBootstrapConfig(project, scope, propName);
        if (bootstrapConfigValue != null) {
            return bootstrapConfigValue;
        }
        PsiFile conf = getScanConfigurationFile(project, scope, null);
        if (conf == null) {
            Notify.getInstance(project).warning(Bundle.getString("notify.error.config.def.notfound", propName));

            if (SERVER_PORT.equals(propName)) {
                return String.valueOf(Settings.HttpToolOptionForm.CONTAINER_PORT.getData());
            } else if (SERVER_SERVLET_CONTEXT_PATH.equals(propName)) {
                String contextPath = Settings.HttpToolOptionForm.CONTAINER_CONTEXT.getData();
                if ("/".equals(contextPath)) {
                    return null;
                }
                return contextPath;
            }
            return null;
        }
        if (conf instanceof PropertiesFile) {
            // application.properties
            PropertiesFile propertiesFile = (PropertiesFile) conf;
            String result = getPropertiesValue(propertiesFile, propName);

            String active = getPropertiesValue(propertiesFile, "spring.profiles.active");
            if (StringUtil.isNotEmpty(active)) {
                conf = getScanConfigurationFile(project, scope, active);
                if (conf instanceof PropertiesFile) {
                    propertiesFile = (PropertiesFile) conf;
                    String readTemp = getPropertiesValue(propertiesFile, propName);
                    if (readTemp != null) {
                        result = readTemp;
                    }
                }
            }
            return result;
        } else if (conf instanceof YAMLFile) {
            // application.yml
            YAMLFile yamlFile = (YAMLFile) conf;

            // 获取application.yml文件默认profile的value
            String result = getYamlValue(yamlFile, propName.split("\\."));

            // 获取application.yml文件默认profile定义的active
            String profileName = getYamlValue(yamlFile, "spring", "profiles", "active");
            if (StringUtil.isNotEmpty(profileName)) {

                // 先查看application.yml中是否定义了多个profile
                List<YAMLDocument> documents = yamlFile.getDocuments();
                if (documents.size() > 1) {
                    for (int i = 1; i < documents.size(); i++) {
                        YAMLDocument yamlDocument = documents.get(i);
                        // 当前定义 profile 的名称
                        String currProfileName = getYamlValue(yamlDocument, "spring", "profiles");
                        if (profileName.equals(currProfileName)) {
                            String currResult = getYamlValue(yamlDocument, propName.split("\\."));
                            if (currResult != null) {
                                result = currResult;
                            }
                        }
                    }
                }
                // 内置profile未找到则寻找 classpath:application-${profileName}.yml
                if ((conf = getScanConfigurationFile(project, scope, profileName)) instanceof YAMLFile) {
                    yamlFile = (YAMLFile) conf;
                    String currResult = getYamlValue(yamlFile, propName.split("\\."));
                    if (currResult != null) {
                        result = currResult;
                    }
                }
            }

            return result;
        }
        return null;
    }

    /**
     * 获取配置文件PsiFile
     *
     * @param project       project
     * @param scope         scope
     * @param qualifiedName 配置文件名（带后缀）
     * @return PsiFile
     */
    @Nullable
    private static PsiFile getConfigurationPsiFile(@NotNull Project project,
                                                   @NotNull GlobalSearchScope scope,
                                                   @NotNull String qualifiedName) {
        try {
            PsiFile[] files = FilenameIndex.getFilesByName(project, qualifiedName, scope);
            // TODO: 有可能出现无法找到文件的情况(files为空数组)
            for (PsiFile file : files) {
                if (file instanceof PropertiesFile || file instanceof YAMLFile) {
                    return file;
                }
            }
        } catch (NoClassDefFoundError e) {
            DumbService.getInstance(project).showDumbModeNotification(Bundle.getString(
                    "notify.error.class.def.notfound",
                    e.getMessage()
            ));
        }
        return null;
    }

    /**
     * 获取扫描到的配置文件
     *
     * @param project project
     * @param scope   scope
     * @param profile ${spring.profiles.active}
     * @return {null | PropertiesFile | YAMLFile}
     */
    @Nullable
    private static PsiFile getScanConfigurationFile(@NotNull Project project,
                                                    @NotNull GlobalSearchScope scope,
                                                    @Nullable String profile) {
        // Spring配置文件名前缀
        final String configurationPrefix = "application" + (StringUtil.isEmpty(profile) ? "" : "-" + profile);

        // 配置文件全名
        final String[] configurationFileNames = {
                // yaml file
                configurationPrefix + "." + YAMLFileType.DEFAULT_EXTENSION,
                // properties file
                configurationPrefix + "." + PropertiesFileType.DEFAULT_EXTENSION,
        };

        for (String configurationFileName : configurationFileNames) {
            PsiFile psiFile = getConfigurationPsiFile(project, scope, configurationFileName);
            if (psiFile != null) {
                return psiFile;
            }
        }
        return null;
    }

    @Nullable
    public static String getConfigValue(@NotNull Object psiFile, @NotNull String propName) {
        if (psiFile instanceof PropertiesFile) {
            return getPropertiesValue(psiFile, propName);
        } else if (psiFile instanceof YAMLFile || psiFile instanceof YAMLDocument) {
            return getYamlValue(psiFile, propName.split("\\."));
        }
        return null;
    }

    @Nullable
    private static String getPropertiesValue(@NotNull Object psiFile, @NotNull String propName) {
        if (psiFile instanceof PropertiesFile) {
            PropertiesFile config = (PropertiesFile) psiFile;
            return config.getNamesMap().get(propName);
        }
        return null;
    }

    @Nullable
    private static String getYamlValue(@NotNull Object psiFile, @NotNull String... propNames) {
        if (psiFile instanceof YAMLFile) {
            YAMLFile config = (YAMLFile) psiFile;
            YAMLKeyValue resultValue = YAMLUtil.getQualifiedKeyInFile(config, propNames);
            if (resultValue != null) {
                return resultValue.getValueText();
            }
        }
        if (psiFile instanceof YAMLDocument) {
            YAMLDocument document = (YAMLDocument) psiFile;
            YAMLKeyValue resultValue = YAMLUtil.getQualifiedKeyInDocument(document, Arrays.asList(propNames));
            if (resultValue != null) {
                return resultValue.getValueText();
            }
        }
        return null;
    }

    public static GlobalSearchScope getModuleScope(@NotNull Module module) {
        return getModuleScope(module, PropertiesKey.scanServiceWithLibrary(module.getProject()));
    }

    protected static GlobalSearchScope getModuleScope(@NotNull Module module, boolean hasLibrary) {
        if (hasLibrary) {
            return module.getModuleWithLibrariesScope();
        } else {
            return module.getModuleScope();
        }
    }
}
