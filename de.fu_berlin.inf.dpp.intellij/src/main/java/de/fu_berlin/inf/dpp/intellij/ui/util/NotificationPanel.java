package de.fu_berlin.inf.dpp.intellij.ui.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

/**
 * Class uses IntelliJ API to show notifications
 */
public class NotificationPanel {
    private static final Logger LOG = Logger.getLogger(NotificationPanel.class);

    public static final String GROUP_NOTIFICATION_ID = "sarosNotification";
    public static final NotificationGroup GROUP_DISPLAY_ID_INFO = new NotificationGroup(
        GROUP_NOTIFICATION_ID, NotificationDisplayType.BALLOON, true);

    @Inject
    private static Project project;

    static {
        SarosPluginContext.initComponent(new NotificationPanel());
    }

    private NotificationPanel() {
    }

    //TODO: Move to core
    /**
     * Displays a notification of the given type.
     * <p/>
     * Possible types are {@link NotificationType#INFORMATION},
     * {@link NotificationType#WARNING} and {@link NotificationType#ERROR}.
     * <p/>
     *
     * @param notificationType type of the notification
     * @param message content of the notification
     * @param title   title of the notification
     */
    private static void showNotification(NotificationType notificationType,
        String message, String title) {

        final Notification notification = GROUP_DISPLAY_ID_INFO
            .createNotification(title, message, notificationType, null);
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                Notifications.Bus.notify(notification, project);
            }
        });

        LOG.info("Notification: " + title + ", " + message);
    }

    /**
     * Display an information notification to the user.
     *
     * @param message content of the notification
     * @param title title of the notification
     */
    public static void showInformation(String message, String title){
        showNotification(NotificationType.INFORMATION, message, title);
    }

    /**
     * Display a warning notification to the user.
     *
     * @param message content of the notification
     * @param title title of the notification
     */
    public static void showWarning(String message, String title){
        showNotification(NotificationType.WARNING, message, title);
    }

    /**
     * Display an error notification to the user.
     *
     * @param message content of the notification
     * @param title title of the notification
     */
    public static void showError(String message, String title){
        showNotification(NotificationType.ERROR, message, title);
    }
}
