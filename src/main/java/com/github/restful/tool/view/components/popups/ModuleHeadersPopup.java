package com.github.restful.tool.view.components.popups;

import com.github.restful.tool.utils.data.ModuleHeaders;
import com.github.restful.tool.utils.data.Storage;
import com.github.restful.tool.view.components.CustomTableModel;
import com.github.restful.tool.view.window.WindowFactory;
import com.github.restful.tool.view.window.frame.Window;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * 模块请求配置弹窗
 * <p>
 * 包括 port | content-path 等
 *
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ModuleHeadersPopup extends JPopupMenu {

    private final transient Project project;
    private final String moduleName;

    private final JTable table;

    private final JButton saveButton;
    private final JButton resetButton;
    private final JButton appendButton;
    private final JButton removeButton;
    private final transient HeadersTableModel tableModel;

    public ModuleHeadersPopup(@NotNull Project project, @NotNull String moduleName) {
        this.project = project;
        this.moduleName = moduleName;

        this.setLayout(new BorderLayout());

        this.saveButton = new JButton("Save");
        this.resetButton = new JButton("Reset");
        this.appendButton = new JButton("Append");
        this.removeButton = new JButton("Remove Line");

        // 构造table
        this.tableModel = new HeadersTableModel();
        this.table = new JBTable(this.tableModel);

        removeButton.addActionListener(e -> {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows == null || selectedRows.length < 1) {
                return;
            }
            for (int row : selectedRows) {
                if (row < 0) {
                    continue;
                }
                tableModel.removeRow(row);
            }
        });

        initLayout();
        initEvent();
    }

    private void initLayout() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setPreferredSize(new Dimension(200, 500));

        // table 列宽设置
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        refreshTable();

        tablePanel.add(table.getTableHeader(), BorderLayout.NORTH);
        tablePanel.add(table, BorderLayout.CENTER);
        this.add(new JBScrollPane(tablePanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(removeButton);
        buttonPanel.add(appendButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(saveButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void initEvent() {
        this.saveButton.addActionListener(e -> {
            Map<String, String> headers = tableModel.getMap();
            // 保存
            ModuleHeaders.setModuleHeader(project, moduleName, headers);
            refreshDocument();

            close();
        });
        this.resetButton.addActionListener(e -> {
            ModuleHeaders.resetModuleHeader(project, moduleName);
            refreshDocument();

            close();
        });
        this.appendButton.addActionListener(e -> {
            tableModel.addRow("", "");
            int rowCount = table.getRowCount();
            table.changeSelection(rowCount - 1, rowCount - 1, false, false);
        });
    }

    private void refreshTable() {
        Map<String, String> configMap = Storage.MODULE_HTTP_HEADER.getMap(project, moduleName);
        tableModel.getDataVector().removeAllElements();
        configMap.entrySet().stream()
                .filter(entry -> {
                    String name = entry.getKey();
                    String value = entry.getValue();
                    return name != null && value != null && !"".equals(name.trim()) && !"".equals(value.trim());
                })
                .forEach(entry -> tableModel.addRow(entry.getKey(), entry.getValue()));
    }

    private void refreshDocument() {
        Window toolWindow = WindowFactory.getToolWindow(project);
        if (toolWindow == null) {
            return;
        }
        toolWindow.refresh();
    }

    private void close() {
        this.setVisible(false);
    }

    private static class HeadersTableModel extends CustomTableModel {

        public HeadersTableModel() {
            super("Name", "Value");
        }
    }
}
