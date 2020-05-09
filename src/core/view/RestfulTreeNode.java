/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RestfullTree
  Author:   ZhangYuanSheng
  Date:     2020/5/6 10:56
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package core.view;

import core.beans.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestfulTreeNode {

    private final List<Request> parents;

    private final List<Request> children;

    private String moduleName;

    public RestfulTreeNode() {
        parents = new ArrayList<>();
        children = new ArrayList<>();
    }

    public RestfulTreeNode(String moduleName) {
        this();
        this.moduleName = moduleName;
    }

    public RestfulTreeNode setParent(Request parent) {
        this.parents.clear();
        this.parents.add(parent);
        return this;
    }

    public RestfulTreeNode appendParent(Request... parents) {
        if (parents != null) {
            this.parents.addAll(Arrays.asList(parents));
        }
        return this;
    }

    public RestfulTreeNode appendChildren(List<? extends Request> item) {
        if (item != null && !item.isEmpty()) {
            this.children.addAll(item);
        }
        return this;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public List<Request> getParents() {
        return this.parents;
    }

    public List<Request> getChildren() {
        return this.children;
    }

}
