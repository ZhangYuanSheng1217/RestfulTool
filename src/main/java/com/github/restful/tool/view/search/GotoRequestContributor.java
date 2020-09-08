/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: GotoRequest
  Author:   ZhangYuanSheng
  Date:     2020/5/12 16:58
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.search;

import com.github.restful.tool.beans.Request;
import com.github.restful.tool.utils.RequestUtil;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class GotoRequestContributor implements ChooseByNameContributor {

    private final Module module;

    private List<RestServiceItem> itemList;

    public GotoRequestContributor(Module module) {
        this.module = module;
    }

    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        List<String> names;

        List<Request> requests;
        if (includeNonProjectItems && module != null) {
            requests = RequestUtil.getModuleRequests(project, module);
        } else {
            requests = new ArrayList<>();
            RequestUtil.getAllRequests(project).forEach((s, rs) -> requests.addAll(rs));
        }
        names = new ArrayList<>(requests.size());

        itemList = new ArrayList<>(requests.size());
        requests.stream().map(request -> new RestServiceItem(
                request.getPsiElement(),
                request.getMethod(),
                request.getPath()
        )).forEach(restServiceItem -> {
            names.add(restServiceItem.getName());
            itemList.add(restServiceItem);
        });

        return names.toArray(new String[0]);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        List<NavigationItem> list = new ArrayList<>();
        itemList.stream()
                .filter(item -> item.getName() != null && item.getName().equals(name))
                .forEach(list::add);

        return list.toArray(new NavigationItem[0]);
    }
}
