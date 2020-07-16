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

import javax.swing.*;
import java.awt.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class OptionForm {

    private final FormBuilder formBuilder;

    private JPanel content;
    private JLabel titleName;
    private JPanel optionItem;

    public OptionForm(@NotNull String titleName) {
        this.titleName.setText(titleName);
        formBuilder = FormBuilder.createFormBuilder();
    }

    protected final void addOptionItem(@NotNull JComponent component) {
        formBuilder.addComponent(component);
    }

    protected final void addOptionItem(@NotNull JComponent component, int topInset) {
        formBuilder.addComponent(component, topInset);
    }

    protected final void addLabeledOptionItem(@NotNull String name, @NotNull JComponent component) {
        formBuilder.addLabeledComponent(name, component);
    }

    protected final void addLabeledOptionItem(@NotNull String name, @NotNull JComponent component, int topInset) {
        formBuilder.addLabeledComponent(name, component, topInset);
    }

    protected final void addLabeledOptionItem(@NotNull String name, @NotNull JComponent component, boolean labelOnTop) {
        formBuilder.addLabeledComponent(name, component, labelOnTop);
    }

    public final JComponent getContent() {
        optionItem.add(formBuilder.getPanel(), BorderLayout.CENTER);
        return content;
    }
}
