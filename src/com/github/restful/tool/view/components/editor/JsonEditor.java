package com.github.restful.tool.view.components.editor;

import com.github.restful.tool.utils.Constants;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.EditorTextField;
import com.intellij.util.LocalTimeCounter;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.border.Border;

/**
 * @author ZhangYuanSheng
 */
public class JsonEditor extends EditorTextField {

    public JsonEditor(Project project) {
        super(null, project, JsonFileType.INSTANCE, false, false);
    }

    public JsonEditor(Project project, FileType fileType) {
        super(null, project, fileType, false, false);
    }

    public static void setupTextFieldEditor(@NotNull EditorEx editor) {
        EditorSettings settings = editor.getSettings();
        settings.setFoldingOutlineShown(true);
        settings.setLineNumbersShown(true);
        settings.setIndentGuidesShown(true);
        editor.setHorizontalScrollbarVisible(true);
        editor.setVerticalScrollbarVisible(true);
    }

    @Override
    protected Document createDocument() {
        final PsiFileFactory factory = PsiFileFactory.getInstance(getProject());
        final long stamp = LocalTimeCounter.currentTime();
        final PsiFile psiFile = factory.createFileFromText(
                Constants.Application.NAME,
                getFileType(),
                "",
                stamp,
                true,
                false
        );
        return PsiDocumentManager.getInstance(getProject()).getDocument(psiFile);
    }

    private void initOneLineMode(@NotNull final EditorEx editor) {
        editor.setOneLineMode(false);
        editor.setColorsScheme(editor.createBoundColorSchemeDelegate(null));
        editor.getSettings().setCaretRowShown(false);
    }

    @Override
    protected EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        initOneLineMode(editor);
        setupTextFieldEditor(editor);
        return editor;
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        super.repaint(tm, x, y, width, height);
        if (getEditor() instanceof EditorEx) {
            initOneLineMode(((EditorEx) getEditor()));
        }
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(JBUI.Borders.empty());
    }
}
