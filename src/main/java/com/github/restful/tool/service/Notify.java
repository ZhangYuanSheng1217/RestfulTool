package com.github.restful.tool.service;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
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

    /**
     * warning-notification
     *
     * @param content 内容
     * @param actions actions
     * @return Notification
     */
    default Notification warning(@NotNull String content, AnAction... actions) {
        Notification notification = warning(content);
        if (actions != null && actions.length > 0) {
            for (AnAction action : actions) {
                notification.addAction(action);
            }
        }
        return notification;
    }

    /**
     * error-notification
     *
     * @param content 内容
     * @param actions actions
     * @return Notification
     */
    default Notification error(@NotNull String content, AnAction... actions) {
        Notification notification = error(content);
        if (actions != null && actions.length > 0) {
            for (AnAction action : actions) {
                notification.addAction(action);
            }
        }
        return notification;
    }

    /**
     * notify
     *
     * @param content 内容
     * @param type    方式
     * @param actions actions
     * @return Notification
     */
    default Notification notify(@NotNull String content, @NotNull NotificationType type, AnAction... actions) {
        Notification notification = notify(content, type);
        if (actions != null && actions.length > 0) {
            for (AnAction action : actions) {
                notification.addAction(action);
            }
        }
        return notification;
    }
}
