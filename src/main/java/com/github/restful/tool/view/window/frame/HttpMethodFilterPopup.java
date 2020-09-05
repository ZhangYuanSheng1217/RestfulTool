/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: HttpMethodFilterPopup
  Author:   ZhangYuanSheng
  Date:     2020/6/1 04:02
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window.frame;

import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.utils.Bundle;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class HttpMethodFilterPopup extends JPopupMenu {

    private final HttpMethod[] values;
    private final List<JBCheckBox> checkBoxList = new ArrayList<>();

    private HttpMethod[] defaultValues;

    @Nullable
    private ChangeCallback changeCallback;
    @Nullable
    private ChangeAllCallback changeAllCallback;

    public HttpMethodFilterPopup(HttpMethod[] values) {
        this(values, values);
    }

    public HttpMethodFilterPopup(@NotNull HttpMethod[] values, HttpMethod[] defaultValues) {
        super();
        this.values = values;
        this.defaultValues = defaultValues;
        initComponent();
    }

    @SuppressWarnings("all")
    private void initComponent() {
        JPanel checkboxPane = new JPanel();
        JPanel buttonPane = new JPanel();
        this.setLayout(new BorderLayout());

        checkboxPane.setLayout(new GridLayout(values.length, 1, 3, 3));
        for (HttpMethod method : values) {
            JBCheckBox checkBox = new JBCheckBox(method.toString(), selected(method));
            checkBox.addActionListener(e -> {
                if (changeCallback != null) {
                    changeCallback.changed(checkBox, method);
                }
            });
            checkBoxList.add(checkBox);
            checkboxPane.add(checkBox);
        }

        JButton selectAll = new JButton(Bundle.getString("other.SelectAll"));
        selectAll.addActionListener(e -> {
            if (getSelectedValues().length < checkBoxList.size()) {
                List<HttpMethod> changes = new ArrayList<>();
                //检查其他的是否被选中乳沟没有就选中他们
                for (int i = 0; i < checkBoxList.size(); i++) {
                    JCheckBox checkBox = checkBoxList.get(i);
                    if (!checkBox.isSelected()) {
                        checkBox.setSelected(true);
                        changes.add(this.values[i]);
                    }
                }
                if (!changes.isEmpty() && changeAllCallback != null) {
                    changeAllCallback.changed(changes, true);
                }
            }
        });
        buttonPane.add(selectAll);

        JButton close = new JButton(Bundle.getString("other.Close"));
        close.addActionListener(e -> this.setVisible(false));
        buttonPane.add(close);

        this.add(checkboxPane, BorderLayout.CENTER);
        this.add(buttonPane, BorderLayout.SOUTH);
    }

    private boolean selected(HttpMethod t) {
        for (HttpMethod defaultValue : defaultValues) {
            if (defaultValue.equals(t)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("all")
    public HttpMethod[] getSelectedValues() {
        List<HttpMethod> selectedValues = new ArrayList<>();

        for (int i = 0; i < checkBoxList.size(); i++) {
            if (checkBoxList.get(i).isSelected()) {
                selectedValues.add(this.values[i]);
            }
        }

        return setDefaultValue(selectedValues.toArray(new HttpMethod[0]));
    }

    public HttpMethod[] setDefaultValue(@NotNull HttpMethod[] defaultValues) {
        this.defaultValues = defaultValues;
        return this.defaultValues;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        setDefaultValue(getSelectedValues());
    }

    public void setChangeCallback(@Nullable ChangeCallback changeCallback) {
        this.changeCallback = changeCallback;
    }

    public void setChangeAllCallback(@Nullable ChangeAllCallback changeAllCallback) {
        this.changeAllCallback = changeAllCallback;
    }

    @Nullable
    public ChangeCallback getChangeCallback() {
        return changeCallback;
    }

    @Nullable
    public ChangeAllCallback getChangeAllCallback() {
        return changeAllCallback;
    }

    public interface ChangeCallback {

        /**
         * 更改回调
         *
         * @param checkBox 单选按钮
         * @param method   数据
         */
        void changed(@NotNull JCheckBox checkBox, @NotNull HttpMethod method);
    }

    public interface ChangeAllCallback {

        /**
         * 更改回调
         *
         * @param selected 选中状态
         * @param methods  数据
         */
        void changed(@NotNull List<HttpMethod> methods, boolean selected);
    }
}
