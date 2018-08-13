package org.optaweb.employeerostering.gwtui.client.notification;

public class NotificationAction {

    private final String i18nKey;
    private final Runnable action;

    public NotificationAction(String i18nKey, Runnable action) {
        this.i18nKey = i18nKey;
        this.action = action;
    }

    public void performAction() {
        action.run();
    }

    public String getI18nKey() {
        return i18nKey;
    }
}
