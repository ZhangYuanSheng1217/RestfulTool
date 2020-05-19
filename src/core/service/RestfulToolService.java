package core.service;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface RestfulToolService {

    /**
     * getInstance
     *
     * @param project project
     * @return obj
     */
    static RestfulToolService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, RestfulToolService.class);
    }

    /**
     * setupImpl
     *
     * @param toolWindow toolWindow
     */
    void setupImpl(@NotNull ToolWindow toolWindow);
}
