package com.github.restful.tool.view.search;

import com.github.restful.tool.beans.HttpMethod;
import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration for file type filtering popup in "Go to | Service" action.
 *
 * @author ZhangYuanSheng
 */
@State(name = "GotoRequestConfiguration", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class GotoRequestConfiguration extends ChooseByNameFilterConfiguration<HttpMethod> {

    /**
     * Get configuration instance
     *
     * @param project a project instance
     * @return a configuration instance
     */
    public static GotoRequestConfiguration getInstance(Project project) {
        return ServiceManager.getService(project, GotoRequestConfiguration.class);
    }

    @Override
    protected String nameForElement(@NotNull HttpMethod type) {
        return type.name();
    }
}
