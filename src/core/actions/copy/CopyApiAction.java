package core.actions.copy;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import core.actions.EditorOption;
import core.beans.Request;
import core.utils.RestUtil;
import core.utils.SystemUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class CopyApiAction extends AnAction implements EditorOption {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(LangDataKeys.PROJECT);
        PsiMethod psiMethod = getPsiMethod();
        if (project == null || psiMethod == null) {
            return;
        }
        Request request = getRequest(project, psiMethod);
        if (request == null) {
            return;
        }
        String contextPath = RestUtil.scanContextPath(project, psiMethod.getResolveScope());
        String path = (contextPath == null || "null".equals(contextPath) ? "" : contextPath) +
                request.getPath();
        SystemUtil.setClipboardString(path);
    }
}
