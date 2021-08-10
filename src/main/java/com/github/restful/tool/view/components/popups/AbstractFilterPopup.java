package com.github.restful.tool.view.components.popups;

import com.github.restful.tool.utils.data.Bundle;
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
public class AbstractFilterPopup<T> extends JPopupMenu {

    private T[] values;
    private List<JBCheckBox> checkBoxList = new ArrayList<>();

    private T[] defaultValues;

    @Nullable
    private ChangeCallback<T> changeCallback;
    @Nullable
    private ChangeAllCallback<T> changeAllCallback;

    public AbstractFilterPopup(T[] values) {
        this(values, values);
    }

    public AbstractFilterPopup(@NotNull T[] values, T[] defaultValues) {
        super();
        render(values, defaultValues);
    }

    public void render(@NotNull T[] values, T[] defaultValues) {
        this.values = values;
        this.defaultValues = defaultValues;
        initComponent();
    }

    public void reset() {
        render(values, values);
    }

    @SuppressWarnings("all")
    private void initComponent() {
        JPanel checkboxPane = new JPanel();
        JPanel buttonPane = new JPanel();
        this.setLayout(new BorderLayout());

        checkboxPane.setLayout(new GridLayout(values.length, 1, 3, 3));
        for (T method : values) {
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

        JButton unSelectAll = new JButton(Bundle.getString("other.UnSelectAll"));
        unSelectAll.addActionListener(e -> {
            List<T> changes = new ArrayList<>();
            for (int i = 0; i < checkBoxList.size(); i++) {
                JCheckBox checkBox = checkBoxList.get(i);
                if (checkBox.isSelected()) {
                    checkBox.setSelected(false);
                    changes.add(this.values[i]);
                }
            }
            if (!changes.isEmpty() && changeAllCallback != null) {
                changeAllCallback.changed(changes, false);
            }
        });
        buttonPane.add(unSelectAll);

        JButton close = new JButton(Bundle.getString("other.Close"));
        close.addActionListener(e -> this.setVisible(false));
        buttonPane.add(close);

        this.add(checkboxPane, BorderLayout.CENTER);
        this.add(buttonPane, BorderLayout.SOUTH);
    }

    public T[] getValues() {
        return this.values;
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

    @Nullable
    public ChangeCallback<T> getChangeCallback() {
        return changeCallback;
    }

    public void setChangeCallback(@Nullable ChangeCallback<T> changeCallback) {
        this.changeCallback = changeCallback;
    }

    @Nullable
    public ChangeAllCallback<T> getChangeAllCallback() {
        return changeAllCallback;
    }

    public void setChangeAllCallback(@Nullable ChangeAllCallback<T> changeAllCallback) {
        this.changeAllCallback = changeAllCallback;
    }

    public interface ChangeCallback<T> {

        /**
         * 更改回调
         *
         * @param checkBox 单选按钮
         * @param module   数据
         */
        void changed(@NotNull JCheckBox checkBox, @NotNull T module);
    }

    public interface ChangeAllCallback<T> {

        /**
         * 更改回调
         *
         * @param selected 选中状态
         * @param modules  数据
         */
        void changed(@NotNull List<T> modules, boolean selected);
    }
}
