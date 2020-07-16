/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RestChooseByNamePopup
  Author:   ZhangYuanSheng
  Date:     2020/5/13 10:48
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.search;

import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestChooseByNamePopup extends ChooseByNamePopup {

    public static final Key<RestChooseByNamePopup> CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY = new Key<>("ChooseByNamePopup");

    protected RestChooseByNamePopup(@Nullable Project project, @NotNull ChooseByNameModel model, @NotNull ChooseByNameItemProvider provider, @Nullable ChooseByNamePopup oldPopup, @Nullable String predefinedText, boolean mayRequestOpenInCurrentWindow, int initialIndex) {
        super(project, model, provider, oldPopup, predefinedText, mayRequestOpenInCurrentWindow, initialIndex);
    }

    @NotNull
    public static RestChooseByNamePopup createPopup(final Project project,
                                                    @NotNull final ChooseByNameModel model,
                                                    @NotNull ChooseByNameItemProvider provider,
                                                    @Nullable final String predefinedText,
                                                    boolean mayRequestOpenInCurrentWindow,
                                                    final int initialIndex) {
        if (!StringUtil.isEmptyOrSpaces(predefinedText)) {
            return new RestChooseByNamePopup(project, model, provider, null, predefinedText, mayRequestOpenInCurrentWindow, initialIndex);
        }

        final RestChooseByNamePopup oldPopup = project == null ? null : project.getUserData(CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY);
        if (oldPopup != null) {
            oldPopup.close(false);
        }
        RestChooseByNamePopup newPopup = new RestChooseByNamePopup(project, model, provider, oldPopup, predefinedText, mayRequestOpenInCurrentWindow, initialIndex);

        if (project != null) {
            project.putUserData(CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY, newPopup);
        }
        return newPopup;
    }

    @NotNull
    public static String getTransformedPattern(@NotNull String pattern, @NotNull ChooseByNameModel model) {
        if (!(model instanceof RequestFilteringGotoByModel)) {
            return pattern;
        }
        pattern = GotoRequestProvider.removeRedundancyMarkup(pattern);
        return pattern;
    }

    @NotNull
    @Override
    public String transformPattern(@NotNull String pattern) {
        final ChooseByNameModel model = getModel();
        return getTransformedPattern(pattern, model);
    }

    @Override
    @Nullable
    public String getMemberPattern() {
        final String enteredText = getTrimmedText();
        final int index = enteredText.lastIndexOf('#');
        if (index == -1) {
            return null;
        }

        String name = enteredText.substring(index + 1).trim();
        return StringUtil.isEmptyOrSpaces(name) ? null : name;
    }
}
