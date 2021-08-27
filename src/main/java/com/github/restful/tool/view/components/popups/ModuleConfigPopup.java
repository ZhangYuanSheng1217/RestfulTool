package com.github.restful.tool.view.components.popups;

import com.github.restful.tool.utils.data.ModuleConfigs;
import com.github.restful.tool.utils.data.Storage;
import com.github.restful.tool.view.components.CustomTableModel;
import com.github.restful.tool.view.window.WindowFactory;
import com.github.restful.tool.view.window.frame.Window;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Map;

import static com.github.restful.tool.utils.data.ModuleConfigs.Config;

/**
 * 模块请求配置弹窗
 * <p>
 * 包括 port | content-path 等
 *
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ModuleConfigPopup extends JBPopupMenu {

    private final transient Project project;
    private final String moduleName;

    private final JButton saveButton;
    private final JButton resetButton;
    private final transient ConfigTableModel tableModel;

    public ModuleConfigPopup(@NotNull Project project, @NotNull String moduleName) {
        this.project = project;
        this.moduleName = moduleName;

        this.setLayout(new BorderLayout());

        this.saveButton = new JButton("Save");
        this.resetButton = new JButton("Reset");

        // 构造table
        this.tableModel = new ConfigTableModel();

        initLayout();
        initEvent();
    }

    private void initLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JTable table = new JBTable(tableModel);
        // table 列宽设置
        TableColumn tableColumn = table.getColumnModel().getColumn(0);
        tableColumn.setPreferredWidth(100);
        tableColumn = table.getColumnModel().getColumn(1);
        tableColumn.setPreferredWidth(100);
        refreshTable();

        mainPanel.add(table.getTableHeader(), BorderLayout.NORTH);
        mainPanel.add(table, BorderLayout.CENTER);
        this.add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(resetButton);
        buttonPanel.add(saveButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void initEvent() {
        this.saveButton.addActionListener(e -> {
            Map<String, String> config = tableModel.getMap();
            setVisible(false);
            // 保存
            ModuleConfigs.setModuleConfig(project, moduleName, config);
            refreshDocument();
        });
        this.resetButton.addActionListener(e -> {
            ModuleConfigs.resetModuleConfig(project, moduleName);
            refreshDocument();
            setVisible(false);
        });
    }

    private void refreshTable() {
        Map<String, String> configMap = Storage.MODULE_HTTP_CONFIG.getMap(project, moduleName);
        Config[] props = Config.values();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            Config prop = props[row];
            tableModel.setName(row, prop.getName());
            tableModel.setValue(row, configMap.getOrDefault(prop.getName(), ""));
        }
    }

    private void refreshDocument() {
        Window toolWindow = WindowFactory.getToolWindow(project);
        if (toolWindow == null) {
            return;
        }
        toolWindow.refresh();
    }

    private static class ConfigTableModel extends CustomTableModel {

        public ConfigTableModel() {
            super(Config.values().length, "Config", "Value");
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            // 首列为配置名 不能修改
            return column == 1;
        }
    }
}
