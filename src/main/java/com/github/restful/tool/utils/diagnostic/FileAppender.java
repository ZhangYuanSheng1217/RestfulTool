///*
//  Copyright (C), 2018-2020, ZhangYuanSheng
//  FileName: FileAppender
//  Author:   ZhangYuanSheng
//  Date:     2020/7/17 17:05
//  Description:
//  History:
//  <author>          <time>          <version>          <desc>
//  作者姓名            修改时间           版本号              描述
// */
//package com.github.restful.tool.utils.diagnostic;
//
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.apache.log4j.PatternLayout;
//import org.jetbrains.annotations.NotNull;
//
///**
// * @author ZhangYuanSheng
// * @version 1.0
// */
//public class FileAppender extends org.apache.log4j.FileAppender {
//
//    public static final String DEFAULT_PATTERN = "[%-5p] %d{yyyy-MM-dd HH:mm:ss,SSS} method:%l%n%m%n";
//
//    public FileAppender(@NotNull String logFile) {
//        this(logFile, DEFAULT_PATTERN);
//    }
//
//    public FileAppender(@NotNull String logFile, @NotNull String pattern) {
//        setFile(logFile);
//        setEncoding("utf-8");
//        setLayout(new PatternLayout(pattern));
//        activateOptions();
//    }
//
//    public void applyDebug(@NotNull Logger logger) {
//        apply(logger, Level.DEBUG);
//    }
//
//    public void applyInfo(@NotNull Logger logger) {
//        apply(logger, Level.INFO);
//    }
//
//    public void applyWarn(@NotNull Logger logger) {
//        apply(logger, Level.WARN);
//    }
//
//    public void applyError(@NotNull Logger logger) {
//        apply(logger, Level.ERROR);
//    }
//
//    public void apply(@NotNull Logger logger, @NotNull Level level) {
//        logger.setLevel(level);
//        logger.addAppender(this);
//    }
//}
