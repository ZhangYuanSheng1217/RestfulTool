/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: JsonTextArea
  Author:   ZhangYuanSheng
  Date:     2020/7/12 21:38
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.components.editor;

import cn.hutool.json.JSONUtil;
import com.github.restful.tool.beans.AppSetting;
import com.github.restful.tool.configuration.AppSettingsState;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * USE https://github.com/bobbylight/RSyntaxTextArea
 *
 * @author ZhangYuanSheng
 * @version 1.0
 * @see RSyntaxTextArea
 */
public class JsonTextArea extends RSyntaxTextArea {

    private final RTextScrollPane scrollPane;

    public JsonTextArea() {
        this.scrollPane = new RTextScrollPane();
        this.scrollPane.setViewportView(this);

        setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);

        initPopupMenu();

        autoSwitchTheme();
    }

    private void initPopupMenu() {
        JMenuItem formatJson = new JMenuItem("Format JSON", AllIcons.Actions.Refresh);
        formatJson.setMnemonic(1);
        formatJson.addActionListener(e -> {
            String responseViewText = getText();
            if (JSONUtil.isJson(responseViewText)) {
                responseViewText = JSONUtil.formatJsonStr(
                        responseViewText.replaceAll("[\n]", "")
                                .replaceAll(" +\"{0}", "")
                );
            }
            setText(responseViewText);
        });
        getPopupMenu().add(formatJson);
    }

    public void applyStyle(int type, Color foreground, Color background, boolean underline) {
        applyStyle(type, foreground, background, null, underline);
    }

    public void applyStyle(int type, Color foreground, Color background, Font font, boolean underline) {
        applyStyle(type, new Style(foreground, background, font, underline));
    }

    public void applyStyle(int type, Style style) {
        SyntaxScheme scheme = getSyntaxScheme();
        scheme.setStyle(type, style);
        revalidate();
        repaint();
    }

    public void applyStyle(@NotNull StyleType styleType) {
        try {
            InputStream resource = RSyntaxTextArea.class.getResourceAsStream(styleType.name);
            Theme theme = Theme.load(resource);
            theme.apply(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自动选择当前主题 {@link StyleType#DARK} | {@link StyleType#DEFAULT}
     *
     * @return 当前应用的主题 {@link StyleType}
     */
    @NotNull
    @SuppressWarnings("UnusedReturnValue")
    public StyleType autoSwitchTheme() {
        AppSetting setting = AppSettingsState.getInstance().getAppSetting();
        boolean darkEditor = EditorColorsManager.getInstance().isDarkEditor();
        String styleType;
        if (darkEditor) {
            styleType = setting.darkStyleType;
        } else {
            styleType = setting.lightStyleType;
        }
        StyleType type = StyleType.parse(styleType, darkEditor);
        applyStyle(type);
        return type;
    }

    public final JScrollPane getScrollPane() {
        return getScrollPane(true);
    }

    public final JScrollPane getScrollPane(boolean lineNumbers) {
        this.scrollPane.setLineNumbersEnabled(lineNumbers);
        return this.scrollPane;
    }
}
