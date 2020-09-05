package com.github.restful.tool.utils.convert;

import com.github.restful.tool.annotation.SpringHttpMethodAnnotation;
import com.github.restful.tool.utils.PsiUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public abstract class BaseConvert<V> {

    private PsiMethod psiMethod;

    /**
     * 是否是基本数据类型
     */
    private boolean isBasicDataTypes;

    /**
     * 是基本数据类型时的参数名
     */
    private String basicDataParamName;

    public BaseConvert() {
    }

    public BaseConvert(@NotNull PsiMethod psiMethod) {
        this.setPsiMethod(psiMethod);
    }

    public void setPsiMethod(@NotNull PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }

    /**
     * 标注了@RequestBody注解则使用application/json格式
     */
    public boolean isRaw() {
        if (psiMethod == null) {
            return false;
        }

        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            for (PsiAnnotation annotation : parameter.getAnnotations()) {
                // 参数标注的注解
                String qualifiedName = annotation.getQualifiedName();
                if (SpringHttpMethodAnnotation.REQUEST_BODY.getQualifiedName().equals(qualifiedName)) {
                    PsiType parameterType = parameter.getType();
                    this.isBasicDataTypes = isBasicDataTypes(parameterType.getCanonicalText());
                    if (this.isBasicDataTypes) {
                        basicDataParamName = parameter.getName();
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isBasicDataTypes(String type) {
        if (type == null) {
            return false;
        }
        final String[] classes = new String[]{
                String.class.getName(),
                Boolean.class.getName(),
                Byte.class.getName(),
                Character.class.getName(),
                Double.class.getName(),
                Float.class.getName(),
                Integer.class.getName(),
                Long.class.getName(),
                Short.class.getName(),
                "boolean",
                "byte",
                "char",
                "double",
                "float",
                "int",
                "long",
                "short",
        };
        for (String clazz : classes) {
            if (clazz.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBasicDataTypes() {
        return isBasicDataTypes;
    }

    public String getBasicDataParamName() {
        return basicDataParamName;
    }

    /**
     * parse method param
     *
     * @return map
     */
    @NotNull
    protected Map<String, V> parseMethodParams() {
        if (psiMethod == null) {
            return Collections.emptyMap();
        }

        PsiParameterList parameterList = psiMethod.getParameterList();
        if (parameterList.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, V> map = new LinkedHashMap<>();
        for (PsiParameter parameter : parameterList.getParameters()) {
            String parameterName = parameter.getName();
            PsiType parameterType = parameter.getType();

            // 如果存在 @RequestParam 注解则获取其注解
            PsiAnnotation annotation = parameter.getAnnotation(SpringHttpMethodAnnotation.REQUEST_PARAM.getQualifiedName());
            if (annotation != null) {
                PsiAnnotationMemberValue attrValue = annotation.findDeclaredAttributeValue("value");
                if (attrValue == null) {
                    attrValue = annotation.findDeclaredAttributeValue("name");
                }
                if (attrValue instanceof PsiLiteralExpressionImpl) {
                    Object value = ((PsiLiteralExpressionImpl) attrValue).getValue();
                    if (value != null) {
                        parameterName = value.toString();
                    }
                }
            }

            Object paramDefaultTypeValue = PsiUtil.getDefaultValueOfPsiType(parameterType);
            if (paramDefaultTypeValue != null) {
                if (paramDefaultTypeValue instanceof Map) {
                    //noinspection unchecked,rawtypes
                    Map<String, V> value = (Map) paramDefaultTypeValue;
                    value.forEach(map::put);
                } else {
                    //noinspection unchecked
                    map.put(parameterName, (V) paramDefaultTypeValue);
                }
            }
        }
        return map;
    }

    /**
     * get method params to convert show String
     *
     * @return str
     */
    public abstract String formatString();

    /**
     * parse show String to convert Key-Value
     *
     * @param paramsStr show string
     * @return map
     */
    public abstract Map<String, V> formatMap(@NotNull String paramsStr);
}
