package com.github.restful.tool.utils;

import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.service.Notify;
import com.github.restful.tool.utils.data.Bundle;
import com.github.restful.tool.view.icon.Icons;
import com.github.restful.tool.view.window.WindowFactory;
import com.github.restful.tool.view.window.frame.Window;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT;
import static com.intellij.openapi.actionSystem.LangDataKeys.EDITOR;
import static com.intellij.openapi.actionSystem.LangDataKeys.PSI_FILE;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class Actions {

    private Actions() {
        // private
    }

    public static void applyActionInfo(@NotNull AnAction action,
                                       @PropertyKey(resourceBundle = Bundle.I18N) String titleKey, @Nullable Icon icon,
                                       @Nullable Object... params) {
        Presentation presentation = action.getTemplatePresentation();
        presentation.setText(Bundle.getString(titleKey, params));
        presentation.setIcon(icon);
    }

    /**
     * 获取当前正在编辑行的elem
     *
     * @param e AnActionEvent
     * @return PsiElement
     */
    @Nullable
    public static PsiElement getCurrentEditorElement(@NotNull AnActionEvent e) {
        Editor editor = e.getData(EDITOR);
        if (editor == null) {
            return null;
        }
        PsiFile psiFile = e.getData(PSI_FILE);
        if (psiFile == null) {
            return null;
        }
        return psiFile.findElementAt(editor.getCaretModel().getOffset());
    }

    /**
     * 检测当前 PsiClass 是否含有`RestController` | `Controller` | `Path`
     *
     * @param psiMethod psiMethod
     * @return bool
     */
    public static boolean hasRestful(@Nullable PsiMethod psiMethod) {
        if (psiMethod == null) {
            return false;
        }
        return ApiServices.hasRestful(psiMethod.getContainingClass());
    }

    /**
     * 获取当前PsiCLass
     *
     * @param event AnActionEvent
     * @return PsiClass
     */
    @NotNull
    public static List<PsiClass> getPsiClass(@NotNull AnActionEvent event) {
        PsiElement currentEditorElement = getCurrentEditorElement(event);
        if (currentEditorElement != null) {
            PsiFile containingFile = currentEditorElement.getContainingFile();
            return PsiUtil.getAllPsiClass(containingFile);
        }
        return Collections.emptyList();
    }

    /**
     * 获取当前选择的PsiMethod
     *
     * @param event AnActionEvent
     * @return PsiMethod
     */
    @Nullable
    public static PsiMethod getPsiMethod(@NotNull AnActionEvent event) {
        PsiElement currentEditorElement = getCurrentEditorElement(event);
        if (currentEditorElement == null) {
            return null;
        }
        // 如果右键处为当前方法其中的 注解末尾 或 方法体中
        PsiElement editorElementContext = currentEditorElement.getContext();
        if (editorElementContext instanceof PsiMethod) {
            return ((PsiMethod) editorElementContext);
        }
        return null;
    }

    /**
     * 是否包含PsiClass
     *
     * @param event AnActionEvent
     * @return bool
     */
    public static boolean withPsiClass(@NotNull AnActionEvent event) {
        if (event.getProject() == null) {
            return false;
        }
        for (PsiClass psiClass : getPsiClass(event)) {
            if (ApiServices.hasRestful(psiClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否包含PsiMethod
     *
     * @param event AnActionEvent
     * @return bool
     */
    public static boolean withPsiMethod(@NotNull AnActionEvent event) {
        if (event.getProject() == null) {
            return false;
        }
        return hasRestful(getPsiMethod(event));
    }

    /**
     * 复制
     *
     * @param event AnActionEvent
     * @param full  全量
     */
    public static void copyPath(@NotNull AnActionEvent event, boolean full) {
        Project project = event.getData(PROJECT);
        PsiMethod psiMethod = getPsiMethod(event);
        if (project == null || psiMethod == null) {
            return;
        }
        List<ApiService> requests = getApiServices(psiMethod);
        if (requests.isEmpty()) {
            Notify.getInstance(project).warning("Cannot find this ApiServices");
            return;
        }
        if (requests.size() == 1) {
            copyPath(psiMethod, requests.get(0), full);
        } else {
            JBPopupFactory.getInstance()
                    .createPopupChooserBuilder(requests)
                    .setItemChosenCallback(selected -> copyPath(psiMethod, selected, full))
                    .setRenderer(new PopupCellRenderer())
                    .setTitle(Bundle.getString("other.prompt.multiItem.selectOne"))
                    .setAdText(JBPopupFactory.ActionSelectionAid.SPEEDSEARCH.name())
                    .setNamerForFiltering(ApiService::getPath)
                    .createPopup()
                    .showInBestPositionFor(event.getDataContext());
        }
    }

    /**
     * 获取指定方法的Apis
     *
     * @param psiMethod psiMethod
     * @return paths
     */
    @NotNull
    private static List<ApiService> getApiServices(@NotNull PsiMethod psiMethod) {
        List<ApiService> apiServices = new ArrayList<>();
        for (ApiService apiService : ApiServices.getCurrClassRequests(psiMethod.getContainingClass())) {
            if (!(apiService.getPsiElement() instanceof PsiMethod)) {
                continue;
            }
            if (apiService.getPsiElement().equals(psiMethod)) {
                apiServices.add(apiService);
            }
        }
        return apiServices;
    }

    /**
     * copy
     *
     * @param psiMethod  psiMethod
     * @param apiService path
     * @param fullPath   full path?
     */
    private static void copyPath(@NotNull PsiMethod psiMethod, @NotNull final ApiService apiService, boolean fullPath) {
        Project project = psiMethod.getProject();
        if (!fullPath) {
            SystemUtil.Clipboard.copy(apiService.getPath());
            Notify.getInstance(project).info(Bundle.getString("action.CopyApi.success"));
        } else {
            SystemUtil.Clipboard.copy(apiService.getRequestUrl());
            Notify.getInstance(project).info(Bundle.getString("action.CopyFullPath.success"));
        }
    }

    public static void enabled(@NotNull AnActionEvent event, @NotNull BooleanSupplier condition) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        event.getPresentation().setEnabled(condition.getAsBoolean());
    }

    private static class PopupCellRenderer extends JLabel implements ListCellRenderer<ApiService> {

        public PopupCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ApiService> list,
                                                      ApiService apiService,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            setText(apiService.getPath());
            setIcon(Icons.getMethodIcon(apiService.getMethod(), isSelected));

            if (isSelected) {
                setBackground(UIUtil.getListSelectionBackground(cellHasFocus));
                setForeground(UIUtil.getListSelectionForeground(cellHasFocus));
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }

    public static void gotoApiServiceTree(@Nullable PsiMethod psiMethod) {
        if (psiMethod == null) {
            return;
        }
        Window window = WindowFactory.getToolWindow(psiMethod.getProject(), true);
        if (window == null) {
            return;
        }
        window.navigationToView(psiMethod);
    }
}
