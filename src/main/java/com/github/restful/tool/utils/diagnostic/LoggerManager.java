///*
//  Copyright (C), 2018-2020, ZhangYuanSheng
//  FileName: LoggerManager
//  Author:   ZhangYuanSheng
//  Date:     2020/7/17 16:54
//  Description:
//  History:
//  <author>          <time>          <version>          <desc>
//  作者姓名            修改时间           版本号              描述
// */
//package com.github.restful.tool.utils.diagnostic;
//
//import org.apache.log4j.Logger;
//import org.jetbrains.annotations.NotNull;
//
///**
// * @author ZhangYuanSheng
// * @version 1.0
// */
//public class LoggerManager {
//
//    /**
//     * 日志文件存储位置
//     */
//    public static final String LOG_FILE = "E://logs/RestfulTool.log";
//
//    private static FileAppender FILE_APPENDER;
//
//    @NotNull
//    private static FileAppender getAppender() {
//        if (FILE_APPENDER == null) {
//            FILE_APPENDER = new FileAppender(LOG_FILE);
//        }
//        return FILE_APPENDER;
//    }
//
//    @NotNull
//    public static Logger getLogger(@NotNull String className) {
//        Logger logger = Logger.getLogger(className);
//        getAppender().applyDebug(logger);
//        return logger;
//    }
//
//    @NotNull
//    public static org.apache.log4j.Logger getLogger(@NotNull Class<?> clazz) {
//        return getLogger(clazz.getName());
//    }
//}
