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
package com.github.restful.tool.actions.copy;

import com.github.restful.tool.utils.Async;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class OptionForEditorGroups extends DefaultActionGroup implements CopyOption {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        Async.runRead(project, () -> withPsiMethod(e), bool -> e.getPresentation().setEnabledAndVisible(bool));
    }
}
