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
import com.github.restful.tool.beans.settings.Settings;
import com.github.restful.tool.view.window.options.Option;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public abstract class BaseInput<T> extends JPanel implements Option {

    public final SettingKey<T> key;
    private final Integer topInset;

    private final JBTextField textField;

    private final Verify<T> verify;

    protected BaseInput(@Nullable T defaultValue, @NotNull SettingKey<T> key, Verify<T> verify, boolean labelOnTop) {
        this(key.getName(), defaultValue, key, null, verify, labelOnTop);
    }

    protected BaseInput(@NotNull String label, @Nullable T defaultValue, @NotNull SettingKey<T> key, Integer topInset, Verify<T> verify, boolean labelOnTop) {
        super(new FlowLayout(FlowLayout.LEFT));
        this.key = key;
        this.topInset = topInset;
        this.textField = new JBTextField(toString(defaultValue));
        this.verify = verify;

        this.add(
                FormBuilder.createFormBuilder()
                        .addLabeledComponent(label, textField, labelOnTop)
                        .getPanel()
        );
    }

    @Override
    public void showSetting(@NotNull Settings setting) {
        this.textField.setText(toString(setting.getData(this.key)));
    }

    @Override
    public void applySetting(@NotNull Settings setting) {
        T value = fromString(textField.getText());
        if (value == null) {
            return;
        }
        if (verify != null && !verify.check(value)) {
            return;
        }
        setting.putData(this.key, value);
    }

    /**
     * T -> String
     *
     * @param data data
     * @return String
     */
    @Nullable
    protected abstract String toString(T data);

    /**
     * String -> T
     *
     * @param data data
     * @return T
     */
    @Nullable
    protected abstract T fromString(String data);

    @Nullable
    @Override
    public Integer getTopInset() {
        return this.topInset;
    }

    protected void appendInputVerify(@NotNull KeyAdapter adapter) {
        this.textField.addKeyListener(adapter);
    }

    protected JTextField getInput() {
        return this.textField;
    }

    public interface Verify<T> {

        /**
         * 验证内容
         *
         * @param data 验证数据
         * @return bool
         */
        boolean check(@Nullable T data);
    }
}
