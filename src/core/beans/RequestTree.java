/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RequestTree
  Author:   ZhangYuanSheng
  Date:     2020/5/4 18:14
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package core.beans;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RequestTree {

    private Request parent;

    private final List<Request> children;

    public RequestTree() {
        children = new ArrayList<>();
    }

    public RequestTree(Request parent) {
        this();
        this.parent = parent;
    }

    public RequestTree setParent(Request parent) {
        this.parent = parent;
        return this;
    }

    public Request getParent() {
        return this.parent;
    }

    public RequestTree addChildren(List<? extends Request> item) {
        if (item != null && !item.isEmpty()) {
            this.children.addAll(item);
        }
        return this;
    }

    @NotNull
    @Contract(" -> new")
    public Vector<Request> getAll() {
        Vector<Request> requests = new Vector<>(children.size());
        children.forEach(request -> {
            if (parent != null) {
                if (parent.getMethod() != null) {
                    request.setMethod(parent.getMethod());
                }
                if (parent.getPath() != null && !"".equals(parent.getPath().trim())) {
                    request.setPath(
                            parent.getPath() + "/" + request.getPath()
                    );
                }
            }
            requests.add(request);
        });
        return requests;
    }
}
