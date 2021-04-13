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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
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
        final String canonicalText = psiType.getCanonicalText();

        // 基本类型  boolean
        if (PsiType.BOOLEAN.equals(psiType) || "java.lang.Boolean".equals(canonicalText)) {
            return false;
        }

        // 基本类型  String
        if (canonicalText.endsWith("java.lang.String")) {
            return "String";
        }

        if (PsiType.LONG.equals(psiType) || "java.lang.Long".equals(canonicalText)) {
            return 0L;
        }

        if (PsiType.DOUBLE.equals(psiType) || "java.lang.Double".equals(canonicalText)) {
            return 0D;
        }

        if (PsiType.FLOAT.equals(psiType) || "java.lang.Float".equals(canonicalText)) {
            return 0F;
        }

        // 基本类型|数字
        if (PsiType.INT.equals(psiType) || "java.lang.Integer".equals(canonicalText)
                || PsiType.BYTE.equals(psiType) || "java.lang.Byte".equals(canonicalText)
                || PsiType.SHORT.equals(psiType) || "java.lang.Short".equals(canonicalText)
                || BigInteger.class.getName().equals(canonicalText)
                || BigDecimal.class.getName().equals(canonicalText)) {
            return 0;
        }

        // 原生的数组
        if (canonicalText.contains("[]")) {
            return new Object[0];
        }

        // 常见的List 和Map
        if (canonicalText.startsWith("java.util.")) {
            if (canonicalText.contains("Map")) {
                return Collections.emptyMap();
            }
            if (canonicalText.contains("List")) {
                return Collections.emptyList();
            }
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

            Map<String, Object> result = new LinkedHashMap<>();

            List<FieldMethod> fieldMethods = PsiUtil.getFieldsMethod(psiClass);
            for (FieldMethod fieldMethod : fieldMethods) {
                PsiField psiField = fieldMethod.getField();
                if (psiField == null) {
                    // 如果该 Getter|Setter 方法所对应的Field为空，则跳过
                    continue;
                }
                PsiType psiFieldType = psiField.getType();
                if (psiFieldType.equals(psiType)) {
                    result.put(fieldMethod.getFieldName(), null);
                    continue;
                }
                result.put(
                        fieldMethod.getFieldName(),
                        getDefaultValueOfPsiType(psiFieldType)
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
        if (qualifiedName.equals(Date.class.getName())) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
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
     * (有序) 获取 PsiClass 中定义的 Getter|Setter 方法，及对应的 Field（可能为空）
     *
     * @param psiClass Class
     * @return FieldsMethods
     * @see PsiUtil.FieldMethod
     */
    @NotNull
    public static List<FieldMethod> getFieldsMethod(@NotNull PsiClass psiClass) {
        LinkedHashMap<String, FieldMethod> map = new LinkedHashMap<>();

        // 读取字段
        for (PsiField field : psiClass.getAllFields()) {
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
                // 如果不是Getter|Setter方法则直接跳过
                continue;
            }

            if (!hasPublicModifier(psiClass.getModifierList()) || hasStaticModifier(psiClass.getModifierList())) {
                // 如果方法修饰符不是public或者方法是静态的则直接跳过
                continue;
            }

            // 截取方法标示的字段名
            final String fieldName = name.substring(3, 4).toLowerCase() + name.substring(4);
            final FieldMethod fieldMethod;
            if (map.containsKey(fieldName)) {
                fieldMethod = map.get(fieldName);
                if (fieldMethod.getField() != null && hasPublicModifier(fieldMethod.getField().getModifierList())) {
                    // 如果字段Field不为空切是public修饰则跳过检查Getter|Setter
                    continue;
                }
            } else {
                PsiField field = psiClass.findFieldByName(fieldName, true);
                if (field == null
                        // 如果是static属性
                        || hasStaticModifier(field.getModifierList())
                        // 如果是final属性
                        || hasFinalModifier(field.getModifierList())) {
                    continue;
                }
                fieldMethod = new FieldMethod(fieldName, field);
                map.put(fieldName, fieldMethod);
            }

            if (name.startsWith(prefixGet)) {
                fieldMethod.addFieldGetter(method);
            } else {
                fieldMethod.addFieldSetter(method);
            }
        }

        return new ArrayList<>(map.values());
    }

    /**
     * 是否是基本数据类型
     *
     * @param type java.lang.String | java.lang.Integer | char | int
     * @return boolean
     */
    public static boolean isBasicDataTypes(String type) {
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

    /**
     * 是否是Kotlin中的基本数据类型
     *
     * @param type java.lang.String | java.lang.Integer | char | int
     * @return boolean
     */
    public static boolean isKotlinBasicDataTypes(String type) {
        if (type == null) {
            return false;
        }
        final String[] classes = new String[]{
                "Boolean",
                "Byte",
                "Int",
                "Short",
                "Long",
                "Float",
                "Double",
                "Char",
                "Number",
                "Array",
                "String"
        };
        for (String clazz : classes) {
            if (clazz.equals(type)) {
                return true;
            }
        }
        return false;
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

        private FieldMethod() {
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
