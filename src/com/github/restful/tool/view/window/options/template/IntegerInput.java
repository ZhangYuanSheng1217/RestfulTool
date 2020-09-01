/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: TextInput
  Author:   ZhangYuanSheng
  Date:     2020/9/1 20:21
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window.options.template;

import com.github.restful.tool.beans.settings.SettingKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class IntegerInput extends BaseInput<Integer> {

    private boolean withDouble = false;

    public IntegerInput(@Nullable Integer defaultValue, @NotNull SettingKey<Integer> key, Verify<Integer> verify, boolean withDouble) {
        super(defaultValue, key, verify, false);
        this.withDouble = withDouble;
        initVerify();
    }

    public IntegerInput(@NotNull String label, @Nullable Integer defaultValue, @NotNull SettingKey<Integer> key, Integer topInset, Verify<Integer> verify, boolean withDouble) {
        super(label, defaultValue, key, topInset, verify, false);
        this.withDouble = withDouble;
        initVerify();
    }

    public IntegerInput(@Nullable Integer defaultValue, @NotNull SettingKey<Integer> key, Verify<Integer> verify) {
        super(defaultValue, key, verify, false);
        initVerify();
    }

    public IntegerInput(@NotNull String label, @Nullable Integer defaultValue, @NotNull SettingKey<Integer> key, Integer topInset, Verify<Integer> verify) {
        super(label, defaultValue, key, topInset, verify, false);
        initVerify();
    }

    private void initVerify() {
        getInput().setColumns(5);

        appendInputVerify(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                char keyChar = event.getKeyChar();
                if (keyChar < KeyEvent.VK_0 || keyChar > KeyEvent.VK_9) {
                    if (withDouble && keyChar == KeyEvent.VK_PERIOD) {
                        Component component = event.getComponent();
                        if (component instanceof JTextField) {
                            JTextField field = (JTextField) component;
                            if (field.getText() != null && field.getText().contains(String.valueOf((char) KeyEvent.VK_PERIOD))) {
                                event.consume();
                            }
                        }
                        return;
                    }
                    event.consume();
                }
            }
        });
    }

    @Nullable
    @Override
    protected String toString(Integer data) {
        return data != null ? data.toString() : null;
    }

    @Nullable
    @Override
    protected Integer fromString(String data) {
        try {
            return Integer.parseInt(data);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
