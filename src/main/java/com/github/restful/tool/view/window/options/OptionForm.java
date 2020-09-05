/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: OptionForm
  Author:   ZhangYuanSheng
  Date:     2020/6/2 10:51
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window.options;

import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class OptionForm {

    private final FormBuilder formBuilder;
    private final int index;
    private JPanel content;
    private JLabel titleName;
    private JPanel optionItem;

    public OptionForm(@NotNull String titleName) {
        this(titleName, 0);
    }

    public OptionForm(@NotNull String titleName, int index) {
        this.titleName.setText(titleName);
        this.index = index;
        formBuilder = FormBuilder.createFormBuilder();
    }

    public final void addOptionItem(@NotNull JComponent component) {
        formBuilder.addComponent(component);
    }

    public final void addOptionItem(@NotNull JComponent component, @Nullable Integer topInset) {
        if (topInset == null) {
            formBuilder.addComponent(component);
            return;
        }
        formBuilder.addComponent(component, topInset);
    }

    public final void addLabeledOptionItem(@NotNull String name, @NotNull JComponent component) {
        formBuilder.addLabeledComponent(name, component);
    }

    public final void addLabeledOptionItem(@NotNull String name, @NotNull JComponent component, int topInset) {
        formBuilder.addLabeledComponent(name, component, topInset);
    }

    public final void addLabeledOptionItem(@NotNull String name, @NotNull JComponent component, boolean labelOnTop) {
        formBuilder.addLabeledComponent(name, component, labelOnTop);
    }

    public final JComponent getContent() {
        optionItem.add(formBuilder.getPanel(), BorderLayout.CENTER);
        return content;
    }

    @NotNull
    public final String getName() {
        return this.titleName.getText();
    }

    public int getIndex() {
        return index;
    }
}
