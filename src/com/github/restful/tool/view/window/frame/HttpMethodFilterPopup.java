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
class HttpMethodFilterPopup<T> extends JPopupMenu {

    private final T[] values;
    private final List<JCheckBox> checkBoxList = new ArrayList<>();

    private T[] defaultValues;

    @Nullable
    private ChangeCallback<T> changeCallback;
    @Nullable
    private ChangeAllCallback<T> changeAllCallback;

    public HttpMethodFilterPopup(T[] values) {
        this(values, values);
    }

    public HttpMethodFilterPopup(@NotNull T[] values, T[] defaultValues) {
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

        for (T t : values) {
            JCheckBox checkBox = new JBCheckBox(t.toString(), selected(t));
            checkBox.addActionListener(e -> {
                if (changeCallback != null) {
                    changeCallback.changed(checkBox, t);
                }
            });
            checkBoxList.add(checkBox);
        }

        checkboxPane.setLayout(new GridLayout(checkBoxList.size(), 1, 3, 3));
        for (JCheckBox box : checkBoxList) {
            checkboxPane.add(box);
        }

        JButton selectAll = new JButton("Select All");
        selectAll.addActionListener(e -> {
            if (getSelectedValues().length < checkBoxList.size()) {
                List<T> changes = new ArrayList<>();
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

        JButton close = new JButton("Close");
        close.addActionListener(e -> this.setVisible(false));
        buttonPane.add(close);

        this.add(checkboxPane, BorderLayout.CENTER);
        this.add(buttonPane, BorderLayout.SOUTH);
    }

    private boolean selected(T t) {
        for (T defaultValue : defaultValues) {
            if (defaultValue.equals(t)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("all")
    public T[] getSelectedValues() {
        List<T> selectedValues = new ArrayList<>();

        for (int i = 0; i < checkBoxList.size(); i++) {
            if (checkBoxList.get(i).isSelected()) {
                selectedValues.add(this.values[i]);
            }
        }

        return setDefaultValue((T[]) selectedValues.toArray());
    }

    public T[] setDefaultValue(@NotNull T[] defaultValues) {
        this.defaultValues = defaultValues;
        return this.defaultValues;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        setDefaultValue(getSelectedValues());
    }

    public void setChangeCallback(@Nullable ChangeCallback<T> changeCallback) {
        this.changeCallback = changeCallback;
    }

    public void setChangeAllCallback(@Nullable ChangeAllCallback<T> changeAllCallback) {
        this.changeAllCallback = changeAllCallback;
    }

    public interface ChangeCallback<T> {

        /**
         * 更改回调
         *
         * @param checkBox 单选按钮
         * @param t        数据
         */
        void changed(@NotNull JCheckBox checkBox, @NotNull T t);
    }

    public interface ChangeAllCallback<T> {

        /**
         * 更改回调
         *
         * @param selected 选中状态
         * @param ts       数据
         */
        void changed(@NotNull List<T> ts, boolean selected);
    }
}
