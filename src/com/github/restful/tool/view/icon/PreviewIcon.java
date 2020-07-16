/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: PreviewIcon
  Author:   ZhangYuanSheng
  Date:     2020/5/31 02:00
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.icon;

import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class PreviewIcon extends JBLabel {

    public PreviewIcon(@NotNull String text, @NotNull Icon icon) {
        super(text, icon, CENTER);
    }
}
