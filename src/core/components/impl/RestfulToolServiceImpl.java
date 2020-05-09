package core.components.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import core.components.RestfulToolService;
import core.view.RestfulTool;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestfulToolServiceImpl implements RestfulToolService {

    private final Project project;

    public RestfulToolServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public void setupImpl(@NotNull ToolWindow toolWindow) {
        System.out.println("project = " + project);

        RestfulTool view = new RestfulTool(project);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(view.getContent(), "", false);

        toolWindow.getContentManager().addContent(content);
    }
}
