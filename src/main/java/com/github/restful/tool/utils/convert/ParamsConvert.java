package com.github.restful.tool.utils.convert;

import com.github.restful.tool.annotation.SpringHttpMethodAnnotation;
import com.github.restful.tool.utils.JsonUtil;
import com.github.restful.tool.utils.PsiUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ParamsConvert {

    private PsiMethod psiMethod;
    private KtNamedFunction function;

    /**
     * 是否是基本数据类型
     */
    private boolean isBasicDataTypes;

    /**
     * 是基本数据类型时的参数名
     */
    private String basicDataParamName;

    public ParamsConvert() {
    }

    public ParamsConvert(@NotNull NavigatablePsiElement psiElement) throws PsiUnSupportException {
        this.setPsiElement(psiElement);
    }

    public void setPsiElement(@NotNull NavigatablePsiElement psiElement) throws PsiUnSupportException {
        if (psiElement instanceof PsiMethod) {
            this.psiMethod = (PsiMethod) psiElement;
            this.function = null;
            return;
        } else if (psiElement instanceof KtNamedFunction) {
            this.function = (KtNamedFunction) psiElement;
            this.psiMethod = null;
            return;
        }
        throw new PsiUnSupportException(psiElement);
    }

    /**
     * 标注了@RequestBody注解则使用application/json格式
     */
    public boolean isRaw() {
        if (psiMethod != null) {
            return isRawOfPsiMethod();
        } else if (function != null) {
            return isRawOfFunction();
        }

        return false;
    }

    private boolean isRawOfPsiMethod() {
        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            for (PsiAnnotation annotation : parameter.getAnnotations()) {
                // 参数标注的注解
                String qualifiedName = annotation.getQualifiedName();
                if (SpringHttpMethodAnnotation.REQUEST_BODY.getQualifiedName().equals(qualifiedName)) {
                    PsiType parameterType = parameter.getType();
                    this.isBasicDataTypes = PsiUtil.isBasicDataTypes(parameterType.getCanonicalText());
                    if (this.isBasicDataTypes) {
                        this.basicDataParamName = parameter.getName();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRawOfFunction() {
        for (KtParameter ktParameter : function.getValueParameters()) {
            for (KtAnnotationEntry annotationEntry : ktParameter.getAnnotationEntries()) {
                Name shortName = annotationEntry.getShortName();
                if (shortName == null) {
                    continue;
                }
                if (SpringHttpMethodAnnotation.REQUEST_BODY.getShortName().equals(shortName.asString())) {
                    KtTypeReference typeReference = ktParameter.getTypeReference();
                    if (typeReference != null) {
                        String type = typeReference.getChildren()[0].getChildren()[0].getText();
                        this.isBasicDataTypes = PsiUtil.isKotlinBasicDataTypes(type);
                        if (this.isBasicDataTypes) {
                            this.basicDataParamName = ktParameter.getName();
                        }
                    }
                    return true;
                }
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
    private Map<String, Object> parseParams() {
        if (psiMethod != null) {
            return parsePsiMethodParams();
        }
        if (function != null) {
            return parseFunctionParams();
        }
        return Collections.emptyMap();
    }

    @NotNull
    private Map<String, Object> parsePsiMethodParams() {
        PsiParameterList parameterList = psiMethod.getParameterList();
        if (parameterList.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> map = new LinkedHashMap<>();
        for (PsiParameter parameter : parameterList.getParameters()) {
            String parameterName = parameter.getName();
            PsiType parameterType = parameter.getType();

            if (parameter.getAnnotation(SpringHttpMethodAnnotation.REQUEST_HEADER.getQualifiedName()) != null) {
                // 如果参数注解带 @RequestHeader 则直接跳过
                continue;
            }

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

            // 如果存在 @PathVariable 注解则获取其注解
            annotation = parameter.getAnnotation(SpringHttpMethodAnnotation.PATH_VARIABLE.getQualifiedName());
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
                } else {
                    parameterName = parameter.getName();
                }
            }

            Object paramDefaultTypeValue = PsiUtil.getDefaultValueOfPsiType(parameterType);
            if (paramDefaultTypeValue != null) {
                if (paramDefaultTypeValue instanceof Map) {
                    //noinspection unchecked,rawtypes
                    Map<String, Object> value = (Map) paramDefaultTypeValue;
                    value.forEach(map::put);
                } else {
                    map.put(parameterName, paramDefaultTypeValue);
                }
            }
        }
        return map;
    }

    @NotNull
    private Map<String, Object> parseFunctionParams() {
        List<KtParameter> parameters = function.getValueParameters();
        if (parameters.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> params = new LinkedHashMap<>();
        base:
        for (KtParameter ktParameter : parameters) {
            String paramName = ktParameter.getName();
            List<KtAnnotationEntry> entries = ktParameter.getAnnotationEntries();
            for (KtAnnotationEntry entry : entries) {
                Name shortName = entry.getShortName();
                if (shortName == null) {
                    continue;
                }
                if (SpringHttpMethodAnnotation.REQUEST_HEADER.getShortName().equals(shortName.asString())) {
                    continue base;
                }
                if (SpringHttpMethodAnnotation.REQUEST_PARAM.getShortName().equals(shortName.asString())) {
                    List<? extends ValueArgument> valueArguments = entry.getValueArguments();
                    if (valueArguments.isEmpty()) {
                        continue base;
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
                                if (!("name".equals(name) || "value".equals(name))) {
                                    continue;
                                }
                            }
                            KtExpression expression = ktValueArgument.getArgumentExpression();
                            if (expression instanceof KtStringTemplateExpression) {
                                KtStringTemplateExpression stringTemplateExpression = (KtStringTemplateExpression) expression;
                                paramName = stringTemplateExpression.getChildren()[0].getText();
                                break;
                            }
                        }
                    }
                }
            }

            params.put(paramName, getDefaultValue(ktParameter.getDefaultValue(), false));
        }
        return params;
    }

    @Nullable
    private Object getDefaultValue(@Nullable KtExpression expression, @SuppressWarnings("SameParameterValue") boolean keep) {
        if (expression == null) {
            return keep ? null : "";
        }
        if (expression instanceof KtStringTemplateExpression) {
            KtStringTemplateExpression templateExpression = (KtStringTemplateExpression) expression;
            String text = templateExpression.getChildren()[0].getText();
            if (!keep && text == null) {
                return "";
            }
            return text;
        }
        if (expression instanceof KtCallExpression) {
            KtCallExpression callExpression = (KtCallExpression) expression;
            return callExpression.getName();
        }
        return keep ? null : "";
    }

    /**
     * get method params to convert show String
     *
     * @return str
     */
    public String formatString() {
        Map<String, Object> methodParams = parseParams();
        return JsonUtil.formatJson(methodParams);
    }

    /**
     * get method params to convert show String
     *
     * @param form form
     * @return str
     */
    public String formatString(@NotNull Map<String, Object> form) {
        return JsonUtil.formatJson(form);
    }

    /**
     * parse show String to convert Key-Value
     *
     * @param paramsStr show string
     * @return map
     */
    public Map<String, Object> formatMap(@NotNull String paramsStr) {
        //noinspection unchecked
        return (Map<String, Object>) JsonUtil.formatMap(paramsStr);
    }
}
