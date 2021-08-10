/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: Constans
  Author:   ZhangYuanSheng
  Date:     2020/6/12 18:39
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.utils.data;

/**
 * 常量池
 *
 * @author ZhangYuanSheng
 * @version 1.0
 */
public final class Constants {

    private Constants() {
        // private
    }

    /**
     * 应用
     */
    public static final class Application {

        /**
         * 插件ID
         */
        public static final String ID = "cn.cloud.auto.restful.tool";

        /**
         * 插件名
         */
        public static final String NAME = Bundle.getString("plugin.name");

        /**
         * 持久化缓存key前缀
         */
        public static final String CONFIG_STORE_PREFIX = "ResultTool:";

        private Application() {
            // private
        }
    }
}
