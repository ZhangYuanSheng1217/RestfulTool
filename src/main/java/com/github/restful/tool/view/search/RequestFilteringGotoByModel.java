/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: GotoRequestFilteringGotoByModel
  Author:   ZhangYuanSheng
  Date:     2020/5/12 17:09
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.search;

import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.utils.Bundle;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.gotoByName.CustomMatcherModel;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RequestFilteringGotoByModel
        extends FilteringGotoByModel<HttpMethod>
        implements DumbAware, CustomMatcherModel {

    protected RequestFilteringGotoByModel(
            @NotNull Project project, @NotNull ChooseByNameContributor[] contributors) {
        super(project, contributors);
    }

    @Nullable
    @Override
    protected HttpMethod filterValueFor(NavigationItem item) {
        if (item instanceof RestServiceItem) {
            return ((RestServiceItem) item).getMethod();
        }
        return null;
    }

    @Override
    public String getPromptText() {
        return Bundle.getString("search.view.Title");
    }

    @NotNull
    @Override
    public String getNotInMessage() {
        return IdeBundle.message("label.no.matches.found", getProject().getName());
    }

    @NotNull
    @Override
    public String getNotFoundMessage() {
        return IdeBundle.message("label.no.matches.found");
    }

    @Nullable
    @Override
    public String getCheckBoxName() {
        return null;
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return false;
    }

    @Override
    public void saveInitialCheckBoxState(boolean state) {
    }

    @NotNull
    @Override
    public String @NotNull [] getSeparators() {
        return new String[]{"/", "?"};
    }

    @Nullable
    @Override
    public String getFullName(@NotNull Object element) {
        return getElementName(element);
    }

    @Override
    public boolean willOpenEditor() {
        return true;
    }

    @Override
    public boolean matches(@NotNull String popupItem, @NotNull String userPattern) {
        return true;
    }
}
