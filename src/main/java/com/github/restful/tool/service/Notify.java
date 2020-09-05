package com.github.restful.tool.service;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface Notify {

    /**
     * instance
     *
     * @param project auto
     * @return this
     */
    static Notify getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, Notify.class);
    }

    /**
     * info-notification
     *
     * @param content 内容
     * @return Notification
     */
    Notification info(@NotNull String content);

    /**
     * warning-notification
     *
     * @param content 内容
     * @return Notification
     */
    Notification warning(@NotNull String content);

    /**
     * error-notification
     *
     * @param content 内容
     * @return Notification
     */
    Notification error(@NotNull String content);

    /**
     * notify
     *
     * @param content 内容
     * @param type    方式
     * @return Notification
     */
    Notification notify(@NotNull String content, @NotNull NotificationType type);
}
