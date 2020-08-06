package com.github.restful.tool.view.window.options;

import com.github.restful.tool.beans.AppSetting;
import com.github.restful.tool.view.window.options.items.SystemOptions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface SettingObserver {

    /**
     * 获得当前组件的设置值，并加载到setting中
     *
     * @param setting setting
     */
    void applySetting(@NotNull final AppSetting setting);

    /**
     * 根据setting中的值获加载到当前组件
     *
     * @param setting setting
     */
    void loadSetting(@NotNull final AppSetting setting);

    /**
     * 添加当前的设置组件
     *
     * @param optionForms forms
     */
    void applyComponent(@NotNull Map<String, OptionForm> optionForms);

    /**
     * 获取当前设置组件的所属条目名
     *
     * @return str
     */
    @NotNull
    default String getOptionFormName() {
        return SystemOptions.NAME;
    }
}
