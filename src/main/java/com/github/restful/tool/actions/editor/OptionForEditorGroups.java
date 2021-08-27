/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: Test
  Author:   ZhangYuanSheng
  Date:     2020/7/7 00:12
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.actions.editor;

import com.github.restful.tool.utils.data.Bundle;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class OptionForEditorGroups extends DefaultActionGroup {

    public OptionForEditorGroups() {
        super(Bundle.getString("plugin.name"), true);
    }
}
