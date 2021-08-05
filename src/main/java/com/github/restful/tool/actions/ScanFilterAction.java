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
package com.github.restful.tool.actions;

import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.utils.Bundle;
import com.github.restful.tool.view.window.frame.HttpMethodFilterPopup;
import com.github.restful.tool.view.window.frame.Window;
import com.intellij.icons.AllIcons;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ScanFilterAction extends AbstractFilterAction<HttpMethod> {

    public ScanFilterAction() {
        super(Bundle.getString("action.ScanFilter.text"), AllIcons.General.Filter, new HttpMethodFilterPopup(Window.getFilterMethods(false)));
    }

    @Override
    protected void callback(HttpMethod item, boolean selected) {
        Window.setMethodFilterStatus(item, selected);
    }
}
