package com.github.restful.tool.view.window.frame;

import cn.hutool.core.util.StrUtil;
import com.github.restful.tool.view.window.WindowFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.*;

public class ModuleUrlPopup extends JPopupMenu {

    private final Project project;
    private final Module[] modules;
    private TableModel tableModel;

    public ModuleUrlPopup(Project project) {
        super();
        this.project = project;
        this.modules = Window.getFilterModules(true);
        initLayout();
    }


    /**
     * 初始化布局
     */
    private void initLayout() {
        this.setLayout(new BorderLayout());
        // 创建内容面板，使用边界布局
        JPanel mainPanel = new JPanel(new BorderLayout());
        JTable table = generateTable();
        mainPanel.add(table.getTableHeader(), BorderLayout.NORTH);
        mainPanel.add(table, BorderLayout.CENTER);
        this.add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(generateSaveButton());
        buttonPanel.add(generateResetButton());
        this.add(buttonPanel, BorderLayout.SOUTH);

    }

    /**
     * 构造数据表格组件
     *
     * @return
     */
    private JTable generateTable() {

        // 构造table
        TableModel tableModel = new DefaultTableModel(new Object[modules.length][2], new Object[]{"Module", "Url Prefix"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 首列为module 不能修改
                return column == 1;
            }
        };
        this.tableModel = tableModel;
        JTable table = new JBTable(tableModel);
        // table 列宽设置
        TableColumn tableColumn = table.getColumnModel().getColumn(0);
        tableColumn.setPreferredWidth(200);
        tableColumn = table.getColumnModel().getColumn(1);
        tableColumn.setPreferredWidth(100);
        // 填充数据
        refreshTable();
        return table;
    }

    /**
     * 保存按钮
     *
     * @return
     */
    private JButton generateSaveButton() {
        JButton selectAll = new JButton("Save");
        selectAll.addActionListener(e -> {
            //从表格中读取数据
            Map<String, String> map = new HashMap<>();
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                map.put((String) tableModel.getValueAt(row, 0), StrUtil.trimToEmpty((String) tableModel.getValueAt(row, 1)));
            }
            // 保存
            ModuleUrlStorageUtil.storeModuleUrlMap(project, map);
            //刷新接口
            refreshApiService();
        });
        return selectAll;
    }


    /**
     * Reset按钮
     *
     * @return
     */
    private JButton generateResetButton() {
        JButton button = new JButton("Reset");
        button.addActionListener(e -> {
            // 清空配置数据
            ModuleUrlStorageUtil.storeModuleUrlMap(project, new HashMap<>());
            // 刷新表格
            refreshTable();
            //刷新接口
            refreshApiService();
        });
        return button;
    }

    /**
     * 读取配置信息，刷新表格中的数据
     */
    private void refreshTable() {
        Map<String, String> moduleUrlMap = ModuleUrlStorageUtil.getModuleUrlMap(project);
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            Module module = modules[row];
            tableModel.setValueAt(module.getName(), row, 0);
            tableModel.setValueAt(moduleUrlMap.getOrDefault(module.getName(), ""), row, 1);
        }
    }

    /**
     * 刷新api信息
     */
    private void refreshApiService() {
        Window toolWindow = WindowFactory.getToolWindow(project);
        if (toolWindow == null) {
            return;
        }
        toolWindow.refreshRequestTree();
    }

}
