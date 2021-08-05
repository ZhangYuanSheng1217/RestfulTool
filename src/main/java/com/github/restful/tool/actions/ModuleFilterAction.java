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

import com.github.restful.tool.view.window.frame.ModuleFilterPopup;
import com.github.restful.tool.view.window.frame.Window;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ModuleFilterAction extends AbstractFilterAction<Module> {

    public ModuleFilterAction() {
        super("Module Filter", AllIcons.General.Filter, new ModuleFilterPopup(Window.getFilterModules(false)));
    }

    @Override
    protected void callback(Module item, boolean selected) {
        Window.setModuleFilterStatus(item, selected);
    }
}
