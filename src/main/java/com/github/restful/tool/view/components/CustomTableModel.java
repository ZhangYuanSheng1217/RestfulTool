package com.github.restful.tool.view.components;

import org.jetbrains.annotations.NotNull;

import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.text.CharSequenceUtil.isEmpty;
import static cn.hutool.core.text.CharSequenceUtil.trimToEmpty;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public abstract class CustomTableModel extends DefaultTableModel {

    public CustomTableModel(@NotNull String column1, @NotNull String column2) {
        this(0, column1, column2);
    }

    public CustomTableModel(int size, @NotNull String column1, @NotNull String column2) {
        super(new Object[size][2], new Object[]{column1, column2});
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    public String getName(int row) {
        return (String) this.getValueAt(row, 0);
    }

    public String getValue(int row) {
        return trimToEmpty((String) this.getValueAt(row, 1));
    }

    public void setName(int row, @NotNull String name) {
        this.setValueAt(name, row, 0);
    }

    public void setValue(int row, @NotNull String value) {
        this.setValueAt(value, row, 1);
    }

    public void addRow(@NotNull String name, @NotNull String value) {
        super.addRow(new Object[]{name, value});
    }

    public void removeAll() {
        super.getDataVector().removeAllElements();
    }

    @NotNull
    public Map<String, String> getMap() {
        Map<String, String> config = new HashMap<>();
        for (int row = 0; row < getRowCount(); row++) {
            String value = getValue(row);
            if (value == null || isEmpty(value)) {
                continue;
            }
            config.put(getName(row), value);
        }
        return config;
    }
}
