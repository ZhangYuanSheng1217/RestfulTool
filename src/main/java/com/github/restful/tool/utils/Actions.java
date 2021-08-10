package com.github.restful.tool.utils;

import com.github.restful.tool.utils.data.Bundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import javax.swing.*;

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
}
