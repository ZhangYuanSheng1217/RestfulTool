/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: PsiUtil
  Author:   ZhangYuanSheng
  Date:     2020/7/21 23:31
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class PsiUtil {

    /**
     * 获取PsiParameter的PsiClass
     *
     * @param psiParameter 参数
     * @return PsiClass
     */
    @Nullable
    public static PsiClass getClassOfPsiParameter(@Nullable PsiParameter psiParameter) {
        if (psiParameter == null) {
            return null;
        }
        PsiTypeElement psiTypeElement = psiParameter.getTypeElement();
        if (psiTypeElement == null) {
            return null;
        }
        PsiJavaCodeReferenceElement referenceElement = psiTypeElement.getInnermostComponentReferenceElement();
        if (referenceElement == null) {
            return null;
        }
        PsiElement psiElement = referenceElement.resolve();
        if (!(psiElement instanceof PsiClass)) {
            return null;
        }
        return (PsiClass) psiElement;
    }

    /**
     * 获取PsiMethod的参数列表
     *
     * @param psiMethod 方法
     * @return PsiParameter
     */
    @NotNull
    public static List<PsiParameter> getParametersOfPsiMethod(@NotNull PsiMethod psiMethod) {
        PsiParameterList parameterList = psiMethod.getParameterList();
        if (parameterList.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(parameterList.getParameters());
    }

    /**
     * 获取PsiType的默认值
     *
     * @return [BasicDataType] | Map | List
     */
    @Nullable
    public static Object getDefaultValueOfPsiType(@NotNull PsiType psiType) {
        if (psiType instanceof PsiArrayType) {
            return Collections.emptyList();
        }
        if (psiType instanceof PsiPrimitiveType) {
            // int | char | boolean
            PsiPrimitiveType type = (PsiPrimitiveType) psiType;
            return getDefaultData(type.getName());
        }
        if (psiType instanceof PsiClassReferenceType) {
            // Object | String | Integer | List<?> | Map<K, V>
            PsiClassReferenceType type = (PsiClassReferenceType) psiType;
            // 通过 Getter|Setter 读取
            PsiClass psiClass = type.resolve();
            if (psiClass == null) {
                return null;
            }

            final Object hasResult = getDefaultData(psiClass);
            if (hasResult != null) {
                return hasResult;
            }

            Map<String, Object> result = new HashMap<>();

            List<FieldMethod> fieldMethods = PsiUtil.getFieldsMethod(psiClass);
            for (FieldMethod fieldMethod : fieldMethods) {
                PsiField psiField = fieldMethod.getField();
                if (psiField == null) {
                    // 如果该 Getter|Setter 方法所对应的Field为空，则跳过
                    continue;
                }
                result.put(
                        fieldMethod.getFieldName(),
                        getDefaultValueOfPsiType(psiField.getType())
                );
            }

            return result;
        }
        return null;
    }

    /**
     * 获取PsiClass的默认值
     *
     * @param psiClass class
     * @return "NULL" | List | Set | Map
     */
    @Nullable
    public static Object getDefaultData(@Nullable PsiClass psiClass) {
        if (psiClass == null) {
            return null;
        }
        String qualifiedName = psiClass.getQualifiedName();
        if (qualifiedName == null) {
            return null;
        }
        if (qualifiedName.equals(List.class.getName())) {
            return Collections.emptyList();
        }
        if (qualifiedName.equals(Set.class.getName())) {
            return Collections.emptySet();
        }
        if (qualifiedName.equals(Map.class.getName())) {
            return Collections.emptyMap();
        }

        final String libPackage = "java.util(.concurrent)?.[a-zA-Z0-9]*";
        @Language("RegExp") final String regList = libPackage + "List";
        @Language("RegExp") final String regSet = libPackage + "Set";
        @Language("RegExp") final String regMap = libPackage + "Map";
        if (Pattern.compile(regList).matcher(qualifiedName).find()) {
            return Collections.emptyList();
        }
        if (Pattern.compile(regSet).matcher(qualifiedName).find()) {
            return Collections.emptySet();
        }
        if (Pattern.compile(regMap).matcher(qualifiedName).find()) {
            return Collections.emptyMap();
        }

        if (Pattern.compile(libPackage).matcher(qualifiedName).find()) {
            return "NULL";
        }

        if (psiClass.getName() != null) {
            return getDefaultData(psiClass.getName());
        }

        return null;
    }

    @Nullable
    public static Object getDefaultData(@NotNull String classType) {
        Object data = null;
        switch (classType.toLowerCase(Locale.ROOT)) {
            case "string":
                data = "demoData";
                break;
            case "char":
            case "character":
                data = 'A';
                break;
            case "byte":
            case "short":
            case "int":
            case "integer":
            case "long":
                data = 0;
                break;
            case "float":
            case "double":
                data = 0.0;
                break;
            case "boolean":
                data = true;
                break;
            default:
                break;
        }
        return data;
    }

    /**
     * 获取 PsiClass 中定义的 Getter|Setter 方法，及对应的 Field（可能为空）
     *
     * @param psiClass Class
     * @return FieldsMethod
     * @see PsiUtil.FieldMethod
     */
    @NotNull
    public static List<FieldMethod> getFieldsMethod(@NotNull PsiClass psiClass) {
        Map<String, FieldMethod> map = new HashMap<>();

        // 读取定义为public的字段
        for (PsiField field : psiClass.getAllFields()) {
            if (!hasPublicModifier(field.getModifierList())) {
                // 如果字段不是public则跳过
                continue;
            }
            if (hasStaticModifier(field.getModifierList()) || hasFinalModifier(field.getModifierList())) {
                // 如果字段是static或final则跳过
                continue;
            }
            map.put(field.getName(), new FieldMethod(field.getName(), field));
        }

        // 读取Getter|Setter方法对应的字段
        for (PsiMethod method : psiClass.getAllMethods()) {
            final String prefixGet = "get";
            final String prefixSet = "Set";
            final String name = method.getName();

            if (name.length() < 4 || !(name.startsWith(prefixGet) || name.startsWith(prefixSet))) {
                continue;
            }

            if (!hasPublicModifier(psiClass.getModifierList()) || hasStaticModifier(psiClass.getModifierList())) {
                // 如果方法修饰符不是public或者方法是静态的则直接跳过
                continue;
            }

            // 获取方法对应的字段
            final String fieldName = name.substring(3, 4).toLowerCase() + name.substring(4);
            final FieldMethod fieldMethod;
            if (map.containsKey(fieldName)) {
                fieldMethod = map.get(fieldName);
            } else {
                fieldMethod = new FieldMethod(fieldName);
                map.put(fieldName, fieldMethod);
            }

            if (name.startsWith(prefixGet)) {
                fieldMethod.addFieldGetter(method);
            } else {
                fieldMethod.addFieldSetter(method);
            }
            PsiField field = psiClass.findFieldByName(fieldMethod.getFieldName(), true);
            if (field == null
                    // 如果是静态属性
                    || hasStaticModifier(field.getModifierList())
                    // 如果是最终属性
                    || hasFinalModifier(field.getModifierList())) {
                continue;
            }
            fieldMethod.setField(field);
        }

        List<FieldMethod> fieldMethods = new ArrayList<>();
        map.forEach((fieldName, fieldMethod) -> fieldMethods.add(fieldMethod));
        return fieldMethods;
    }

    /**
     * 是否是公共(public)方法
     *
     * @param target PsiModifierList的实现类
     * @return bool
     * @see PsiUtil#hasModifier(PsiModifierList, String)
     */
    public static boolean hasPublicModifier(@Nullable PsiModifierList target) {
        return PsiUtil.hasModifier(target, PsiModifier.PUBLIC);
    }

    /**
     * 是否是私有(private)方法
     *
     * @param target PsiModifierList的实现类
     * @return bool
     * @see PsiUtil#hasModifier(PsiModifierList, String)
     */
    public static boolean hasPrivateModifier(@Nullable PsiModifierList target) {
        return PsiUtil.hasModifier(target, PsiModifier.PRIVATE);
    }

    /**
     * 是否是静态(static)方法
     *
     * @param target PsiModifierList的实现类
     * @return bool
     * @see PsiUtil#hasModifier(PsiModifierList, String)
     */
    public static boolean hasStaticModifier(@Nullable PsiModifierList target) {
        return PsiUtil.hasModifier(target, PsiModifier.STATIC);
    }

    /**
     * 是否是最终(final)方法
     *
     * @param target PsiModifierList的实现类
     * @return bool
     * @see PsiUtil#hasModifier(PsiModifierList, String)
     */
    public static boolean hasFinalModifier(@Nullable PsiModifierList target) {
        return PsiUtil.hasModifier(target, PsiModifier.FINAL);
    }

    /**
     * 是否具有指定修饰符
     *
     * @param target PsiModifierList的实现类
     * @return bool
     */
    public static boolean hasModifier(@Nullable PsiModifierList target, @PsiModifier.ModifierConstant @NotNull String modifier) {
        if (target == null) {
            return false;
        }
        // 是否具有修饰符属性
        return target.hasModifierProperty(modifier);
    }

    public static class FieldMethod {

        /**
         * Getter方法, 可能有多个
         */
        private final List<PsiMethod> fieldGetters;
        /**
         * Setter方法, 可能有多个
         */
        private final List<PsiMethod> fieldSetters;
        private String fieldName;
        private PsiField field;

        /**
         * Getter的无参方法
         */
        private PsiMethod noParameterMethodOfGetter;

        public FieldMethod() {
            fieldGetters = new ArrayList<>();
            fieldSetters = new ArrayList<>();
        }

        public FieldMethod(@NotNull String fieldName) {
            this(fieldName, null);
        }

        public FieldMethod(@NotNull String fieldName, @Nullable PsiField field) {
            this();
            this.fieldName = fieldName;
            this.field = field;
        }

        @NotNull
        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(@NotNull String fieldName) {
            this.fieldName = fieldName;
        }

        @Nullable
        public PsiField getField() {
            return field;
        }

        public void setField(@Nullable PsiField field) {
            this.field = field;
        }

        @NotNull
        public List<PsiMethod> getFieldGetters() {
            return fieldGetters;
        }

        public void addFieldGetter(@NotNull PsiMethod fieldGetter) {
            this.fieldGetters.add(fieldGetter);
            if (fieldGetter.getParameterList().isEmpty()) {
                this.setNoParameterMethodOfGetter(fieldGetter);
            }
        }

        @NotNull
        public List<PsiMethod> getFieldSetters() {
            return fieldSetters;
        }

        public void addFieldSetter(@NotNull PsiMethod fieldSetter) {
            this.fieldSetters.add(fieldSetter);
        }

        public boolean emptyGetter() {
            return getFieldGetters().isEmpty();
        }

        public boolean emptySetter() {
            return getFieldSetters().isEmpty();
        }

        @Nullable
        public PsiMethod getNoParameterMethodOfGetter() {
            return noParameterMethodOfGetter;
        }

        public void setNoParameterMethodOfGetter(@NotNull PsiMethod noParameterMethod) {
            this.noParameterMethodOfGetter = noParameterMethod;
        }
    }
}
