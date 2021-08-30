package com.github.restful.tool.actions.intention;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.restful.tool.service.Notify;
import com.github.restful.tool.utils.SystemUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class Json2ClassIntentionAction extends BaseIntentionAction {

    protected Json2ClassIntentionAction() {
        super("Generate class fields form JSON.");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiClass psiClass = findClass(element);
        if (psiClass == null) {
            return;
        }

        String text = SystemUtil.Clipboard.paste();
        if (text == null || "".equals(text) || !JSONUtil.isJsonObj(text)) {
            Notify.getInstance(project).info("Failed to get JSON from clipboard.");
            return;
        }
        WriteCommandAction.runWriteCommandAction(project, () -> generate(project, psiClass, text));
    }

    private void generate(@NotNull Project project, @NotNull PsiClass psiClass, String text) {
        JSONObject json = JSONUtil.parseObj(text);

        json.forEach((name, value) -> {
            String valueType = getValueType(value);

            PsiField fieldByName = psiClass.findFieldByName(name, false);
            if (fieldByName != null) {
                return;
            }
            PsiField psiField = generateField(project, name, valueType);
            psiClass.addAfter(psiField, psiClass.getLBrace());

            PsiMethod[] methods = generateGetterAndSetter(project, name, valueType);
            PsiMethod getter = methods[0];
            if (psiClass.findMethodBySignature(getter, false) == null) {
                psiClass.addBefore(getter, psiClass.getRBrace());
            }
            PsiMethod setter = methods[1];
            if (psiClass.findMethodBySignature(setter, false) == null) {
                psiClass.addBefore(setter, psiClass.getRBrace());
            }
        });
    }

    @NotNull
    private String getValueType(Object value) {
        String valueType = "Object";
        if (value instanceof Boolean) {
            valueType = "Boolean";
        } else if (value instanceof Integer) {
            valueType = "Integer";
        } else if (value instanceof Byte) {
            valueType = "Byte";
        } else if (value instanceof Date) {
            valueType = "Date";
        } else if (value instanceof Long) {
            valueType = "Long";
        } else if (value instanceof Character) {
            valueType = "Character";
        } else if (value instanceof Short) {
            valueType = "Short";
        } else if (value instanceof String) {
            valueType = "String";
        }
        return valueType;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return findClass(element) != null;
    }

    @NotNull
    private PsiField generateField(@NotNull Project project, @NotNull String fieldName, @NotNull String fieldType) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        return factory.createFieldFromText(String.format("private %s %s;", fieldType, fieldName), null);
    }

    private PsiMethod @NotNull [] generateGetterAndSetter(@NotNull Project project, @NotNull String fieldName, @NotNull String type) {
        PsiMethod[] methods = new PsiMethod[2];

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        // Getter
        methods[0] = factory.createMethodFromText(String.format(
                "public %s get%s() { return this.%s; }",
                type, upperCaseFirstWord(fieldName), fieldName
        ), null);
        // Setter
        methods[1] = factory.createMethodFromText(String.format(
                "public void set%s(%s %s) { this.%s = %s; }",
                upperCaseFirstWord(fieldName), type, fieldName, fieldName, fieldName
        ), null);

        return methods;
    }

    @NotNull
    private String upperCaseFirstWord(@NotNull String word) {
        if (word.length() < 1) {
            throw new IllegalArgumentException();
        }
        if (word.length() == 1) {
            return word.toUpperCase();
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}
