/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RestfulToolWindow
  Author:   ZhangYuanSheng
  Date:     2020/4/29 15:06
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package core.view.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import core.service.RestfulToolService;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestfulToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        RestfulToolService.getInstance(project).setupImpl(toolWindow);
    }
}
