package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class NotificationSystem {

    @Inject
    private NotificationMessage notification;

    public void notify(String title, String message) {
        notification.withTitle(title)
                .withMessage(message)
                .show();
    }
}
