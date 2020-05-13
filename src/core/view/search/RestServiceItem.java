package core.view.search;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.MinusculeMatcher;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.search.GlobalSearchScope;
import core.beans.RequestMethod;
import core.utils.RestUtil;
import core.view.Icons;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 */
public class RestServiceItem implements NavigationItem {

    private final PsiElement psiElement;

    private PsiMethod psiMethod;
    private Module module;
    private RequestMethod method;

    private String url;

    private Navigatable navigationElement;

    public RestServiceItem(PsiElement psiElement, String requestMethod, String urlPath) {
        this(psiElement, RequestMethod.valueOf(requestMethod), urlPath);
    }

    public RestServiceItem(PsiElement psiElement, RequestMethod method, String urlPath) {
        this.psiElement = psiElement;
        if (psiElement instanceof PsiMethod) {
            this.psiMethod = (PsiMethod) psiElement;
        }
        if (method != null) {
            this.method = method;
        }
        this.url = urlPath;
        if (psiElement instanceof Navigatable) {
            navigationElement = (Navigatable) psiElement;
        }
    }

    @Nullable
    @Override
    public String getName() {
        return this.url;
    }

    @Nullable
    @Override
    @Contract(" -> new")
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {

            @Nullable
            @Override
            public String getPresentableText() {
                return getUrl();
            }

            @Override
            public String getLocationString() {
                String location = null;

                if (psiElement instanceof PsiMethod) {
                    PsiMethod psiMethod = ((PsiMethod) psiElement);
                    PsiClass psiClass = psiMethod.getContainingClass();
                    if (psiClass != null) {
                        location = psiClass.getName();
                    }
                    location += "#" + psiMethod.getName();
                }

                return "(" + location + ")";
            }

            @NotNull
            @Override
            public Icon getIcon(boolean unused) {
                return Icons.getMethodIcon(method);
            }
        };
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (navigationElement != null) {
            navigationElement.navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return navigationElement.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    /**
     * 匹配
     */
    public boolean matches(String queryText) {
        if ("/".equals(queryText)) {
            return true;
        }

        MinusculeMatcher matcher = NameUtil.buildMatcher("*" + queryText, NameUtil.MatchingCaseSensitivity.NONE);
        return matcher.matches(this.url);
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public PsiMethod getPsiMethod() {
        return psiMethod;
    }

    public void setPsiMethod(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFullUrl() {
        if (this.psiMethod != null) {
            Project project = this.psiMethod.getProject();
            GlobalSearchScope scope = this.psiMethod.getResolveScope();
            String protocol = RestUtil.scanListenerProtocol(project, scope);
            int port = RestUtil.scanListenerPort(project, scope);
            System.out.println("protocol = " + protocol);
            System.out.println("port = " + port);
            return protocol + "://localhost:" + port + getUrl();
        }

        return getUrl();
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }
}
