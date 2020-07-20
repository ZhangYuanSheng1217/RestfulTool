/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: EditorTextPanel
  Author:   ZhangYuanSheng
  Date:     2020/7/18 22:45
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.components.editor;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorColorsUtil;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.ComponentUtil;
import com.intellij.ui.EditorTextComponent;
import com.intellij.ui.TextAccessor;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.LocalTimeCounter;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

/**
 * @author ZhangYuanSheng
 * @see com.intellij.ui.EditorTextField
 */
public class JsonEditor extends NonOpaquePanel implements EditorTextComponent, DocumentListener, TextAccessor {

    private final Project myProject;
    private final List<DocumentListener> myDocumentListeners = ContainerUtil.createLockFreeCopyOnWriteList();
    private FileType myFileType;
    private Document myDocument;
    private EditorEx myEditor;
    private boolean myWholeTextSelected;
    private boolean myIsListenerInstalled;
    private boolean myIsViewer;
    private Color myEnforcedBgColor;
    private boolean myEnsureWillComputePreferredSize;
    private Dimension myPassivePreferredSize;

    public JsonEditor(Project project) {
        this(null, project, JsonFileType.INSTANCE);
    }

    public JsonEditor(Project project, FileType fileType) {
        this(null, project, fileType);
    }

    private JsonEditor(Document document, Project project, FileType fileType) {
        myIsViewer = false;
        setDocument(document);
        myProject = project;
        myFileType = fileType;
        setLayout(new BorderLayout());
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        setFocusable(true);

        setFont(UIManager.getFont("TextField.font"));
    }

    private static void doSelectAll(@NotNull Editor editor) {
        editor.getCaretModel().removeSecondaryCarets();
        editor.getCaretModel().getPrimaryCaret().setSelection(0, editor.getDocument().getTextLength(), false);
    }

    public static void setupTextFieldEditor(@NotNull EditorEx editor) {
        EditorSettings settings = editor.getSettings();
        settings.setAdditionalLinesCount(0);
        settings.setAdditionalColumnsCount(1);
        settings.setRightMarginShown(false);
        settings.setRightMargin(-1);
        settings.setFoldingOutlineShown(true);
        settings.setLineNumbersShown(true);
        settings.setLineMarkerAreaShown(false);
        settings.setIndentGuidesShown(true);
        settings.setVirtualSpace(false);
        settings.setWheelFontChangeEnabled(false);
        settings.setAdditionalPageAtBottom(false);
        editor.setHorizontalScrollbarVisible(true);
        editor.setVerticalScrollbarVisible(true);
        settings.setLineCursorWidth(1);
    }

    public void setFileType(@Nullable FileType fileType) {
        if (fileType == null) {
            this.myFileType = FileTypes.PLAIN_TEXT;
        } else {
            this.myFileType = fileType;
        }
    }

    @NotNull
    @Override
    public String getText() {
        return myDocument.getText();
    }

    @Override
    public void setText(@Nullable final String text) {
        CommandProcessor.getInstance().executeCommand(
                getProject(),
                () -> ApplicationManager.getApplication().runWriteAction(() -> {
                    myDocument.replaceString(0, myDocument.getTextLength(), StringUtil.notNullize(text));
                    if (myEditor != null) {
                        final CaretModel caretModel = myEditor.getCaretModel();
                        if (caretModel.getOffset() >= myDocument.getTextLength()) {
                            caretModel.moveToOffset(myDocument.getTextLength());
                        }
                    }
                }),
                null, null,
                UndoConfirmationPolicy.DEFAULT,
                getDocument()
        );
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void addDocumentListener(@NotNull DocumentListener listener) {
        myDocumentListeners.add(listener);
        installDocumentListener();
    }

    @Override
    public void removeDocumentListener(@NotNull DocumentListener listener) {
        myDocumentListeners.remove(listener);
        uninstallDocumentListener(false);
    }

    @Override
    public void beforeDocumentChange(@NotNull DocumentEvent event) {
        for (DocumentListener documentListener : myDocumentListeners) {
            documentListener.beforeDocumentChange(event);
        }
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        for (DocumentListener documentListener : myDocumentListeners) {
            documentListener.documentChanged(event);
        }
    }

    public Project getProject() {
        return myProject;
    }

    @Override
    @NotNull
    public Document getDocument() {
        if (myDocument == null) {
            myDocument = createDocument();
        }
        return myDocument;
    }

    public void setDocument(Document document) {
        if (myDocument != null) {
            uninstallDocumentListener(true);
        }

        myDocument = document;
        installDocumentListener();
        if (myEditor != null) {
            //MainWatchPanel监视oldEditor的焦点，以便在焦点丢失时删除调试器组合框
            //我们应该先将焦点转移到新的oldEditor，然后再删除当前的oldEditor
            //MainWatchPanel检查oldEditor.getParent == newEditor.getParent，并且在这种情况下不删除oldEditor

            boolean isFocused = isFocusOwner();
            EditorEx newEditor = createEditor();
            releaseEditor(myEditor);
            myEditor = newEditor;
            add(myEditor.getComponent(), BorderLayout.CENTER);

            validate();
            if (isFocused) {
                IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(newEditor.getContentComponent(), true));
            }

            myEditor.getComponent().setBorder(JBUI.Borders.empty(16));
        }
    }

    private void installDocumentListener() {
        if (myDocument != null && !myDocumentListeners.isEmpty() && !myIsListenerInstalled) {
            myIsListenerInstalled = true;
            myDocument.addDocumentListener(this);
        }
    }

    private void uninstallDocumentListener(boolean force) {
        if (myDocument == null) {
            return;
        }
        if (myIsListenerInstalled && (force || myDocumentListeners.isEmpty())) {
            myIsListenerInstalled = false;
            myDocument.removeDocumentListener(this);
        }
    }

    @Override
    public void addNotify() {
        if (myProject != null) {
            ProjectManagerListener listener = new ProjectManagerListener() {
                @Override
                public void projectClosing(@NotNull Project project) {
                    releaseEditor(myEditor);
                    myEditor = null;
                }
            };
            ProjectManager.getInstance().addProjectManagerListener(myProject, listener);
        }

        if (myEditor != null) {
            releaseEditorLater();
        }

        boolean isFocused = isFocusOwner();

        initEditor();

        super.addNotify();

        revalidate();
        if (isFocused) {
            IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(this::requestFocus);
        }
    }

    private void initEditor() {
        myEditor = createEditor();
        myEditor.getContentComponent().setEnabled(isEnabled());
        String tooltip = getToolTipText();
        if (StringUtil.isNotEmpty(tooltip)) {
            myEditor.getContentComponent().setToolTipText(tooltip);
        }
        add(myEditor.getComponent(), BorderLayout.CENTER);
    }

    private void releaseEditor(Editor editor) {
        if (editor == null) {
            return;
        }

        if (myProject != null && !myProject.isDisposed() && myIsViewer) {
            final PsiFile psiFile = PsiDocumentManager.getInstance(myProject).getPsiFile(editor.getDocument());
            if (psiFile != null) {
                DaemonCodeAnalyzer.getInstance(myProject).setHighlightingEnabled(psiFile, true);
            }
        }

        remove(editor.getComponent());

        if (!editor.isDisposed()) {
            EditorFactory.getInstance().releaseEditor(editor);
        }
    }

    void releaseEditorLater() {
        EditorEx editor = myEditor;
        ApplicationManager.getApplication().invokeLater(() -> releaseEditor(editor), ModalityState.stateForComponent(this));
        myEditor = null;
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (myEditor != null) {
            setupEditorFont(myEditor);
        }
    }

    private void initOneLineMode(@NotNull final EditorEx editor) {
        // set mode in editor
        editor.setOneLineMode(false);

        editor.setColorsScheme(editor.createBoundColorSchemeDelegate(null));

        editor.getSettings().setCaretRowShown(false);
    }

    protected Document createDocument() {
        final PsiFileFactory factory = PsiFileFactory.getInstance(myProject);
        final long stamp = LocalTimeCounter.currentTime();
        final PsiFile psiFile = factory.createFileFromText("Dummy." + myFileType.getDefaultExtension(), myFileType, "", stamp, true, false);
        return PsiDocumentManager.getInstance(myProject).getDocument(psiFile);
    }

    protected EditorEx createEditor() {
        Document document = getDocument();
        final EditorFactory factory = EditorFactory.getInstance();
        EditorEx editor = (EditorEx) (myIsViewer ? factory.createViewer(document, myProject) : factory.createEditor(document, myProject));

        setupTextFieldEditor(editor);
        editor.setCaretEnabled(!myIsViewer);

        if (myProject != null) {
            PsiFile psiFile = PsiDocumentManager.getInstance(myProject).getPsiFile(editor.getDocument());
            if (psiFile != null) {
                DaemonCodeAnalyzer.getInstance(myProject).setHighlightingEnabled(psiFile, !myIsViewer);
            }
        }

        if (myProject != null) {
            EditorHighlighterFactory highlighterFactory = EditorHighlighterFactory.getInstance();
            VirtualFile virtualFile = myDocument == null ? null : FileDocumentManager.getInstance().getFile(myDocument);
            EditorHighlighter highlighter = virtualFile != null ? highlighterFactory.createEditorHighlighter(myProject, virtualFile) :
                    myFileType != null ? highlighterFactory.createEditorHighlighter(myProject, myFileType) : null;
            if (highlighter != null) {
                editor.setHighlighter(highlighter);
            }
        }

        editor.getSettings().setCaretRowShown(false);

        editor.setOneLineMode(false);
        editor.getCaretModel().moveToOffset(document.getTextLength());

        if (!shouldHaveBorder()) {
            editor.setBorder(null);
        }

        if (myIsViewer) {
            editor.getSelectionModel().removeSelection();
        } else if (myWholeTextSelected) {
            doSelectAll(editor);
            myWholeTextSelected = false;
        }

        editor.getContentComponent().setFocusCycleRoot(false);

        initOneLineMode(editor);

        return editor;
    }

    private void setupEditorFont(@NotNull final EditorEx editor) {
        ((EditorImpl) editor).setUseEditorAntialiasing(false);
        editor.getColorsScheme().setEditorFontName(getFont().getFontName());
        editor.getColorsScheme().setEditorFontSize(getFont().getSize());
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(JBUI.Borders.empty());
    }

    protected boolean shouldHaveBorder() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (isEnabled() != enabled) {
            super.setEnabled(enabled);
            setFocusTraversalPolicyProvider(enabled);
            setViewerEnabled(enabled);
            EditorEx editor = myEditor;
            if (editor != null) {
                releaseEditor(editor);
                initEditor();
                revalidate();
            }
        }
    }

    protected void setViewerEnabled(boolean enabled) {
        myIsViewer = !enabled;
    }

    @Override
    public Color getBackground() {
        Color color = getBackgroundColor(isEnabled(), EditorColorsUtil.getGlobalOrDefaultColorScheme());
        return color != null ? color : super.getBackground();
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        myEnforcedBgColor = bg;
        if (myEditor != null) {
            myEditor.setBackgroundColor(bg);
        }
    }

    private Color getBackgroundColor(boolean enabled, final EditorColorsScheme colorsScheme) {
        if (myEnforcedBgColor != null) {
            return myEnforcedBgColor;
        }
        CellRendererPane parentOfType = ComponentUtil.getParentOfType(
                (Class<? extends CellRendererPane>) CellRendererPane.class,
                (Component) this
        );
        if (parentOfType != null) {
            if (StartupUiUtil.isUnderDarcula() || UIUtil.isUnderIntelliJLaF()) {
                return getParent().getBackground();
            }
        }

        if (StartupUiUtil.isUnderDarcula()) {
            return UIUtil.getTextFieldBackground();
        }

        return enabled ? colorsScheme.getDefaultBackground() : UIUtil.getInactiveTextFieldBackgroundColor();
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        assert myEditor != null && comp == myEditor.getComponent() : "You are not allowed to add anything to EditorTextField";

        super.addImpl(comp, constraints, index);
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }

        boolean toReleaseEditor = false;
        if (myEditor == null && myEnsureWillComputePreferredSize) {
            myEnsureWillComputePreferredSize = false;
            initEditor();
            toReleaseEditor = true;
        }

        Dimension size = JBUI.size(100, 10);
        if (myEditor != null) {
            Dimension preferredSize = myEditor.getComponent().getPreferredSize();

            JBInsets.addTo(preferredSize, getInsets());
            size = preferredSize;
        } else if (myPassivePreferredSize != null) {
            size = myPassivePreferredSize;
        }

        if (toReleaseEditor) {
            releaseEditor(myEditor);
            myEditor = null;
            myPassivePreferredSize = size;
        }

        return size;
    }

    @Override
    public Dimension getMinimumSize() {
        if (isMinimumSizeSet()) {
            return super.getMinimumSize();
        }

        Dimension size = JBUI.size(1, 10);
        if (myEditor != null) {
            size.height = myEditor.getLineHeight();

            if (UIUtil.isUnderDefaultMacTheme() || StartupUiUtil.isUnderDarcula() || UIUtil.isUnderIntelliJLaF()) {
                size.height = Math.max(size.height, JBUIScale.scale(16));
            }

            JBInsets.addTo(size, getInsets());
            JBInsets.addTo(size, myEditor.getInsets());
        }

        return size;
    }

    /**
     * @return null if the editor is not initialized (e.g. if the field is not added to a container)
     * @see #createEditor()
     * @see #addNotify()
     */
    @Nullable
    public Editor getEditor() {
        return myEditor;
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        super.repaint(tm, x, y, width, height);
        if (myEditor != null) {
            initOneLineMode(myEditor);
        }
    }
}
