package org.optaweb.employeerostering.gwtui.client.notification;

public enum NotificationType {

    INFO("alert-info", "pficon-info"),
    WARNING("alert-warning", "pficon-warning-triangle-o"),
    SUCCESS("alert-success", "pficon-ok"),
    ERROR("alert-danger", "pficon-error-circle-o");

    private String notificationTypeClass;
    private String notificationIconClass;

    private NotificationType(String notificationTypeClass, String notificationIconClass) {
        this.notificationTypeClass = notificationTypeClass;
        this.notificationIconClass = notificationIconClass;
    }

    public String getNotificationTypeClass() {
        return notificationTypeClass;
    }

    public String getNotificationIconClass() {
        return notificationIconClass;
    }
}
