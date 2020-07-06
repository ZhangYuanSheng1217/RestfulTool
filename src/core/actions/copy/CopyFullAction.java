package core.actions.copy;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import core.actions.EditorOption;
import core.beans.Request;
import core.utils.RestUtil;
import core.utils.SystemUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class CopyFullAction extends AnAction implements EditorOption {

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
        GlobalSearchScope scope = psiMethod.getResolveScope();
        String contextPath = RestUtil.scanContextPath(project, scope);
        String path = RestUtil.getRequestUrl(
                RestUtil.scanListenerProtocol(project, scope),
                RestUtil.scanListenerPort(project, scope),
                contextPath,
                request.getPath());
        SystemUtil.setClipboardString(path);
    }
}
