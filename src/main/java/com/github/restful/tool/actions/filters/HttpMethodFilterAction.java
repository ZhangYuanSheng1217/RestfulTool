/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RefreshAction
  Author:   ZhangYuanSheng
  Date:     2020/8/18 15:34
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.actions.filters;

import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.view.icon.Icons;
import com.github.restful.tool.view.window.frame.Window;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class HttpMethodFilterAction extends AbstractFilterAction<HttpMethod> {

    public HttpMethodFilterAction() {
        super("action.HttpMethodFilter.text", Icons.Filter_HttpMethod, () -> Window.getFilterMethods(false));
    }

    @Override
    protected void callback(HttpMethod item, boolean selected) {
        Window.setMethodFilterStatus(item, selected);
    }
}
