package com.github.restful.tool.view.components.popups;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ModuleFilterPopup extends AbstractFilterPopup<Module> {

    public ModuleFilterPopup(Module[] values) {
        super(values);
    }

    public void render(@NotNull Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        super.render(modules, modules);
    }
}
