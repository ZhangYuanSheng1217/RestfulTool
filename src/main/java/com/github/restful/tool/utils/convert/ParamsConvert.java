package com.github.restful.tool.utils.convert;

import com.github.restful.tool.annotation.SpringHttpMethodAnnotation;
import com.github.restful.tool.utils.PsiUtil;
import com.github.restful.tool.utils.data.JsonUtil;
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
import java.util.stream.Collectors;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ParamsConvert {

    private ParamsConvert() {
    }

    /**
     * get method params to convert show String
     *
     * @return str
     */
    public static String formatString(NavigatablePsiElement psiElement) {
        Map<String, Object> methodParams = null;
        if (psiElement instanceof PsiMethod) {
            methodParams = parsePsiMethodParams(((PsiMethod) psiElement));
        } else if (psiElement instanceof KtNamedFunction) {
            methodParams = parseFunctionParams(((KtNamedFunction) psiElement));
        }
        if (methodParams == null) {
            return "";
        }
        return JsonUtil.formatJson(methodParams);
    }

    @NotNull
    private static Map<String, Object> parsePsiMethodParams(PsiMethod psiMethod) {
        PsiParameterList parameterList = psiMethod.getParameterList();
        if (parameterList.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> map = new LinkedHashMap<>();
        for (PsiParameter parameter : parameterList.getParameters()) {
            parsePsiMethodParams(map, parameter);
        }
        return map;
    }

    private static void parsePsiMethodParams(Map<String, Object> map, PsiParameter parameter) {
        final String nameKey = "name";
        final String valueKey = "value";

        String parameterName = parameter.getName();
        PsiType parameterType = parameter.getType();

        if (parameter.getAnnotation(SpringHttpMethodAnnotation.REQUEST_HEADER.getQualifiedName()) != null) {
            // 如果参数注解带 @RequestHeader 则直接跳过
            return;
        }

        // 如果存在 @RequestParam 注解则获取其注解
        PsiAnnotation requestParam = parameter.getAnnotation(SpringHttpMethodAnnotation.REQUEST_PARAM.getQualifiedName());
        if (requestParam != null) {
            PsiAnnotationMemberValue attrValue = requestParam.findDeclaredAttributeValue(valueKey);
            if (attrValue == null) {
                attrValue = requestParam.findDeclaredAttributeValue(nameKey);
            }
            if (attrValue instanceof PsiLiteralExpressionImpl) {
                Object value = ((PsiLiteralExpressionImpl) attrValue).getValue();
                if (value != null) {
                    parameterName = value.toString();
                }
            }
        }

        // 如果存在 @PathVariable 注解则获取其注解
        PsiAnnotation pathVariable = parameter.getAnnotation(SpringHttpMethodAnnotation.PATH_VARIABLE.getQualifiedName());
        if (pathVariable != null) {
            PsiAnnotationMemberValue attrValue = pathVariable.findDeclaredAttributeValue(valueKey);
            if (attrValue == null) {
                attrValue = pathVariable.findDeclaredAttributeValue(nameKey);
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
                map.putAll((Map) paramDefaultTypeValue);
            } else {
                map.put(parameterName, paramDefaultTypeValue);
            }
        }
    }

    @NotNull
    private static Map<String, Object> parseFunctionParams(KtNamedFunction function) {
        List<KtParameter> parameters = function.getValueParameters();
        if (parameters.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> params = new LinkedHashMap<>();

        parameters.forEach(ktParameter -> {
            String paramName = ktParameter.getName();
            List<KtAnnotationEntry> entries = ktParameter.getAnnotationEntries();
            for (KtAnnotationEntry entry : entries) {
                Name shortName = entry.getShortName();
                if (shortName == null) {
                    continue;
                }
                if (SpringHttpMethodAnnotation.REQUEST_HEADER.getShortName().equals(shortName.asString())) {
                    return;
                }
                if (SpringHttpMethodAnnotation.REQUEST_PARAM.getShortName().equals(shortName.asString())) {
                    List<? extends ValueArgument> valueArguments = entry.getValueArguments();
                    if (valueArguments.isEmpty()) {
                        return;
                    } else {
                        paramName = parseFunctionParams(paramName, valueArguments);
                    }
                }
            }

            params.put(paramName, getDefaultValue(ktParameter.getDefaultValue(), false));
        });

        return params;
    }

    private static String parseFunctionParams(String paramName, List<? extends ValueArgument> valueArguments) {
        if (valueArguments == null || valueArguments.isEmpty()) {
            return paramName;
        }
        List<KtValueArgument> collect = valueArguments.stream()
                .filter(KtValueArgument.class::isInstance)
                .map(KtValueArgument.class::cast)
                .filter(ktValueArgument -> {
                    if (ktValueArgument.isNamed()) {
                        KtValueArgumentName argumentName = ktValueArgument.getArgumentName();
                        if (argumentName == null) {
                            return false;
                        }
                        String name = argumentName.getAsName().asString();
                        return "name".equals(name) || "value".equals(name);
                    }
                    return true;
                })
                .collect(Collectors.toList());
        for (KtValueArgument ktValueArgument : collect) {
            KtExpression expression = ktValueArgument.getArgumentExpression();
            if (expression instanceof KtStringTemplateExpression) {
                KtStringTemplateExpression stringTemplateExpression = (KtStringTemplateExpression) expression;
                paramName = stringTemplateExpression.getChildren()[0].getText();
                break;
            }
        }
        return paramName;
    }

    @Nullable
    private static Object getDefaultValue(@Nullable KtExpression expression, @SuppressWarnings("SameParameterValue") boolean keep) {
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
}
