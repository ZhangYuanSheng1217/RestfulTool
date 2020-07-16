package com.github.restful.tool.service.impl;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.github.restful.tool.service.Notify;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class NotificationImpl implements Notify {

    private final NotificationGroup NOTIFICATION_GROUP;

    private final Project project;

    public NotificationImpl(@NotNull Project project) {
        this.NOTIFICATION_GROUP = new NotificationGroup("RestfulTool Notification", NotificationDisplayType.BALLOON, true);
        this.project = project;
    }

    @Override
    public Notification info(@NotNull String content) {
        return notify(content, NotificationType.INFORMATION);
    }

    @Override
    public Notification warning(@NotNull String content) {
        return notify(content, NotificationType.WARNING);
    }

    @Override
    public Notification error(@NotNull String content) {
        return notify(content, NotificationType.ERROR);
    }

    @Override
    public Notification notify(@NotNull String content, @NotNull NotificationType type) {
        final Notification notification = NOTIFICATION_GROUP.createNotification(content, type);
        notification.notify(project);
        return notification;
    }
}
