package com.github.restful.tool.actions;

import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import cn.hutool.core.util.CharsetUtil;
import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.utils.ApiServices;
import com.github.restful.tool.utils.data.Bundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 *
 * @version 1.0
 */
public class OutPutAction extends DumbAwareAction {

    public OutPutAction() {
        getTemplatePresentation().setText(Bundle.getString("action.OutPutAction.text"));
        getTemplatePresentation().setIcon(AllIcons.Actions.Download);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor(), null, null,
                new Consumer<VirtualFile>() {
                    @Override
                    public void consume(VirtualFile virtualFile) {
                        Map<String, List<ApiService>> apis = ApiServices.getApis(project);

                        String path;
                        if (virtualFile.isDirectory()) {
                            path = virtualFile.getPath() + "request.csv";
                        } else {
                            path = virtualFile.getPath();
                        }

                        String[] rows = new String[]{
                                "模块", "接口"
                        };

                        CsvWriter writer = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);

                        writer.write(rows);

                        for (Map.Entry<String, List<ApiService>> entry : apis.entrySet()) {

                            rows[0] = entry.getKey();

                            for (ApiService service : entry.getValue()) {
                                rows[1] =   service.getPath();
                                writer.write(rows);
                            }
                        }

                        writer.close();
                    }
                });
    }
}
