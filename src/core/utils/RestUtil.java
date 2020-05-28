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
package core.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.intellij.lang.jvm.annotation.*;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import core.beans.Request;
import core.beans.HttpMethod;
import core.annotation.SpringRequestMethodAnnotation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestUtil {

    private static final int REQUEST_TIMEOUT = 1000 * 10;

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
            String value = getConfigurationValue(
                    getScanConfigurationFile(project, scope),
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
            String value = getConfigurationValue(getScanConfigurationFile(project, scope), "server.ssl.enabled");
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
            return getConfigurationValue(
                    getScanConfigurationFile(project, scope),
                    "server.servlet.context-path"
            );
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * 发送http请求
     *
     * @param method 请求方式
     * @param url    地址
     * @param head   请求头
     * @param body   请求体
     * @return 返回结果
     */
    public static String sendRequest(HttpMethod method, String url, String head, String body) {
        String resp;
        try {
            HttpRequest request = HttpUtil.createRequest(Method.valueOf(method.name()), url);

            if (head != null && !"".equals(head.trim())) {
                tempDataCoverToMap(head).forEach(request::header);
            }
            if (body != null && !"".equals(body.trim())) {
                tempDataCoverToMap(body).forEach(request::form);
            }

            resp = request.timeout(REQUEST_TIMEOUT).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
            resp = e.getMessage();
        }
        return resp;
    }

    @NotNull
    @Contract(pure = true)
    public static Map<String, String> tempDataCoverToMap(String tempData) {
        Map<String, String> map = new HashMap<>();

        if (tempData != null && !"".equals((tempData = tempData.trim()))) {
            String[] items = tempData.split("\n");
            for (String item : items) {
                String[] data = item.split(":");
                if (data.length == 2) {
                    map.put(data[0].trim(), data[1].trim());
                }
            }
        }

        return map;
    }

    /**
     * 获取所有的Request
     *
     * @param project project
     * @return map-{key: moduleName, value: itemRequestList}
     */
    @NotNull
    public static Map<String, List<Request>> getAllRequest(@NotNull Project project) {
        return getAllRequest(project, false);
    }

    /**
     * 获取所有的Request
     *
     * @param hasEmpty 是否生成包含空Request的moduleName
     * @param project  project
     * @return map-{key: moduleName, value: itemRequestList}
     */
    @NotNull
    public static Map<String, List<Request>> getAllRequest(@NotNull Project project, boolean hasEmpty) {
        Map<String, List<Request>> map = new HashMap<>();

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            List<Request> requests = getAllRequestByModule(project, module);
            if (requests.isEmpty() && !hasEmpty) {
                continue;
            }
            map.put(module.getName(), requests);
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
    public static List<Request> getAllRequestByModule(@NotNull Project project, @NotNull Module module) {
        List<Request> moduleList = new ArrayList<>(0);

        List<PsiClass> controllers = RestUtil.getAllControllerClass(project, module);
        if (controllers.isEmpty()) {
            return moduleList;
        }

        for (PsiClass controllerClass : controllers) {
            List<Request> parentRequests = new ArrayList<>(0);
            List<Request> childrenRequests = new ArrayList<>();
            PsiAnnotation psiAnnotation = controllerClass.getAnnotation(
                    SpringRequestMethodAnnotation.REQUEST_MAPPING.getQualifiedName()
            );
            if (psiAnnotation != null) {
                parentRequests = RestUtil.getRequests(psiAnnotation, null);
            }

            PsiMethod[] psiMethods = controllerClass.getMethods();
            for (PsiMethod psiMethod : psiMethods) {
                childrenRequests.addAll(RestUtil.getRequests(psiMethod));
            }
            if (parentRequests.isEmpty()) {
                moduleList.addAll(childrenRequests);
            } else {
                parentRequests.forEach(parentRequest -> childrenRequests.forEach(childrenRequest -> {
                    Request request = childrenRequest.copyWithParent(parentRequest);
                    moduleList.add(request);
                }));
            }
        }
        return moduleList;
    }

    /**
     * 获取方法参数
     *
     * @param method method
     */
    @NotNull
    public static String getRequestParamsTempData(@NotNull PsiMethod method) {
        StringBuilder tempData = new StringBuilder();

        PsiParameterList parameterList = method.getParameterList();
        if (!parameterList.isEmpty()) {
            for (PsiParameter parameter : parameterList.getParameters()) {
                PsiAnnotation[] parameterAnnotations = parameter.getAnnotations();
                String parameterName = parameter.getName();
                PsiType parameterType = parameter.getType();

                boolean flag = true;

                for (PsiAnnotation parameterAnnotation : parameterAnnotations) {
                    if (!SpringRequestMethodAnnotation.REQUEST_PARAM.getQualifiedName().equals(parameterAnnotation.getQualifiedName())) {
                        continue;
                    }
                    List<JvmAnnotationAttribute> attributes = parameterAnnotation.getAttributes();
                    for (JvmAnnotationAttribute attribute : attributes) {
                        String name = attribute.getAttributeName();
                        if (!("name".equals(name) || "value".equals(name))) {
                            continue;
                        }
                        Object value = RestUtil.getAttrValue(attribute.getAttributeValue());
                        if (value instanceof String) {
                            parameterName = ((String) value);
                            flag = !flag;
                        }
                    }
                }

                Object data = RestUtil.getTypeDefaultData(method, parameterType);

                if (data != null) {
                    if (flag) {
                        tempData.append(parameterName).append(": ");
                    }
                    tempData.append(data).append("\n");
                }
            }
        }
        return tempData.toString();
    }

    /**
     * 获取url
     *
     * @param protocol    协议
     * @param port        端口
     * @param contextPath 访问根目录名
     * @param path        路径
     * @return url
     */
    @NotNull
    public static String getRequestUrl(@NotNull String protocol, @Nullable Integer port, @Nullable String contextPath, String path) {
        StringBuilder url = new StringBuilder(protocol + "://");
        url.append("localhost");
        if (port != null) {
            url.append(":").append(port);
        }
        if (contextPath != null && !"null".equals(contextPath) && contextPath.startsWith("/")) {
            url.append(contextPath);
        }
        if (!path.startsWith("/")) {
            url.append("/");
        }
        url.append(path);
        return url.toString();
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

        Collection<PsiAnnotation> pathList = JavaAnnotationIndex.getInstance().get(
                Control.Controller.getName(),
                project,
                module.getModuleScope()
        );
        pathList.addAll(JavaAnnotationIndex.getInstance().get(
                Control.RestController.getName(),
                project,
                module.getModuleScope()
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
     * @see RestUtil#getRequests(PsiMethod)
     */
    @NotNull
    private static List<Request> getRequests(@NotNull PsiAnnotation annotation, @Nullable PsiMethod psiMethod) {
        SpringRequestMethodAnnotation spring = SpringRequestMethodAnnotation.getByQualifiedName(
                annotation.getQualifiedName()
        );
        if (spring == null) {
            return Collections.emptyList();
        }
        Set<String> methods = new HashSet<>();
        methods.add(spring.getMethod() == null ? "ALL" : spring.getMethod().name());
        List<String> paths = new ArrayList<>();

        // 是否为隐式的path（未定义value或者path）
        boolean hasImplicitPath = true;
        List<JvmAnnotationAttribute> attributes = annotation.getAttributes();
        for (JvmAnnotationAttribute attribute : attributes) {
            String name = attribute.getAttributeName();

            if (methods.contains("ALL") && "method".equals(name)) {
                // method可能为数组
                Object value = getAttrValue(attribute.getAttributeValue());
                if (value instanceof String) {
                    methods.add((String) value);
                } else if (value instanceof List) {
                    //noinspection unchecked,rawtypes
                    List<String> list = (List) value;
                    methods.addAll(list);
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
            Object value = getAttrValue(attribute.getAttributeValue());
            if (value instanceof String) {
                paths.add(((String) value));
            } else if (value instanceof List) {
                //noinspection unchecked,rawtypes
                List<Object> list = (List) value;
                list.forEach(item -> paths.add((String) item));
            }
            hasImplicitPath = false;
        }
        if (hasImplicitPath) {
            if (psiMethod != null) {
                // paths.add(psiMethod.getName());
                paths.add("/");
            }
        }

        List<Request> requests = new ArrayList<>(paths.size());

        paths.forEach(path -> {
            for (String method : methods) {
                HttpMethod rm = null;
                if ("ALL".equals(method)) {
                    if (methods.size() > 1) {
                        continue;
                    }
                } else {
                    rm = HttpMethod.valueOf(method);
                }
                requests.add(new Request(
                        rm,
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
        PsiAnnotation[] annotations = method.getModifierList().getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            requests.addAll(RestUtil.getRequests(annotation, method));
        }

        return requests;
    }

    @Nullable
    private static Object getTypeDefaultData(@NotNull PsiMethod method, PsiType parameterType) {
        Object data = null;
        if (parameterType instanceof PsiArrayType) {
            data = "[]";
        } else if (parameterType instanceof PsiClassReferenceType) {
            // Object | String | Integer | List<?> | Map<K, V>
            PsiClassReferenceType type = (PsiClassReferenceType) parameterType;

            GlobalSearchScope resolveScope = type.getResolveScope();
            PsiFile[] psiFiles = FilenameIndex.getFilesByName(
                    method.getProject(),
                    type.getName() + ".java",
                    resolveScope
            );
            if (psiFiles.length > 0) {
                for (PsiFile psiFile : psiFiles) {
                    if (psiFile instanceof PsiJavaFile) {
                        PsiClass[] fileClasses = ((PsiJavaFile) psiFile).getClasses();
                        StringBuilder item = new StringBuilder();
                        for (PsiClass psiClass : fileClasses) {
                            if (type.getReference().getQualifiedName().equals(psiClass.getQualifiedName())) {
                                PsiField[] fields = psiClass.getFields();
                                for (PsiField field : fields) {
                                    String fieldName = field.getName();
                                    Object defaultData = getTypeDefaultData(method, field.getType());
                                    item.append(fieldName).append(": ").append(defaultData).append("\n");
                                }
                                break;
                            }
                        }
                        data = item.toString();
                        break;
                    }
                }
            } else {
                data = getDefaultData(type.getName());
            }
        } else if (parameterType instanceof PsiPrimitiveType) {
            // int | char | boolean
            PsiPrimitiveType type = (PsiPrimitiveType) parameterType;
            data = getDefaultData(type.getName());
        }
        return data;
    }

    @Contract(pure = true)
    private static Object getDefaultData(@NotNull String classType) {
        Object data = null;
        switch (classType) {
            case "String":
                data = "demoData";
                break;
            case "char":
            case "Char":
                data = 'A';
                break;
            case "byte":
            case "short":
            case "int":
            case "long":
            case "Byte":
            case "Short":
            case "Integer":
            case "Long":
                data = 0;
                break;
            case "float":
            case "double":
            case "Float":
            case "Double":
                data = 0.0;
                break;
            case "boolean":
            case "Boolean":
                data = true;
                break;
            default:
                break;
        }
        return data;
    }

    /**
     * 获取属性值
     *
     * @param attributeValue Psi属性
     * @return {Object | List}
     */
    private static Object getAttrValue(JvmAnnotationAttributeValue attributeValue) {
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
                list.add(getAttrValue(value));
            }
            return list;
        }
        return null;
    }

    /**
     * 获取扫描到的配置文件
     *
     * @param project project
     * @param scope   scope
     * @return {null | PropertiesFile | YAMLFile}
     */
    @Nullable
    private static PsiFile getScanConfigurationFile(@NotNull Project project, @NotNull GlobalSearchScope scope) {
        // Spring配置文件名前缀
        final String configurationPrefix = "application";

        // 配置文件全名
        final String[] configurationFileNames = {
                // properties file
                configurationPrefix + "." + PropertiesFileType.DEFAULT_EXTENSION,
                // yaml file
                configurationPrefix + "." + YAMLFileType.DEFAULT_EXTENSION,
        };

        try {
            for (String configurationFileName : configurationFileNames) {
                PsiFile[] files = FilenameIndex.getFilesByName(project, configurationFileName, scope);

                for (PsiFile file : files) {
                    if (file instanceof PropertiesFile) {
                        // application.properties
                        return file;
                    } else if (file instanceof YAMLFile) {
                        // application.yml
                        return file;
                    }
                }
            }
        } catch (NoClassDefFoundError e) {
            DumbService.getInstance(project).showDumbModeNotification(String.format(
                    "IDE is missing the corresponding package file: %s",
                    e.getMessage()
            ));
        }
        return null;
    }

    /**
     * 获取properties或yaml文件的kv值
     *
     * @param conf PsiFile
     * @param name name
     * @return {value | null}
     */
    @Nullable
    private static String getConfigurationValue(@Nullable PsiFile conf, @NotNull String name) {
        if (conf == null) {
            return null;
        }
        if (conf instanceof PropertiesFile) {
            // application.properties
            PropertiesFile propertiesFile = (PropertiesFile) conf;
            return propertiesFile.getNamesMap().get(name);
        } else if (conf instanceof YAMLFile) {
            // application.yml
            YAMLFile yamlFile = (YAMLFile) conf;

            YAMLKeyValue server = YAMLUtil.getQualifiedKeyInFile(
                    yamlFile,
                    name.split("\\.")
            );
            if (server != null) {
                return server.getValueText();
            }
        }
        return null;
    }

    enum Control {

        /**
         * <p>@Controller</p>
         */
        Controller("Controller"),

        /**
         * <p>@RestController</p>
         */
        RestController("RestController");

        private final String name;

        Control(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
