/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.notification;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.logging.impl.StackTracePrintStream;
import elemental2.dom.HTMLDivElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;

@Templated
@ApplicationScoped
public class NotificationFactory
        implements
        IsElement {

    @Inject
    @DataField("notifications")
    private HTMLDivElement notifications;

    @Inject
    private ManagedInstance<ToastNotification> toastNotificationInstance;

    public void showInfoMessage(String i18nKey, Object... notificationArgs) {
        ToastNotification notification = toastNotificationInstance.get();
        notification.setup(NotificationType.INFO, i18nKey, notificationArgs);
        notifications.insertBefore(notification.getElement(), notifications.firstChild);
    }

    public void showWarningMessage(String i18nKey, Object... notificationArgs) {
        ToastNotification notification = toastNotificationInstance.get();
        notification.setup(NotificationType.WARNING, i18nKey, notificationArgs);
        notifications.insertBefore(notification.getElement(), notifications.firstChild);
    }

    public void showErrorMessage(String i18nKey, Object... notificationArgs) {
        ToastNotification notification = toastNotificationInstance.get();
        notification.setup(NotificationType.ERROR, i18nKey, notificationArgs);
        notifications.insertBefore(notification.getElement(), notifications.firstChild);
    }

    public void showError(Throwable throwable) {
        while (throwable instanceof UmbrellaException) {
            UmbrellaException ue = (UmbrellaException) throwable;
            if (ue.getCauses().size() == 1) {
                throwable = ue.getCauses().iterator().next();
            } else {
                break;
            }
        }

        StringBuilder message = new StringBuilder();
        StackTracePrintStream stackTracePrintStream = new StackTracePrintStream(message);
        throwable.printStackTrace(stackTracePrintStream);

        ToastNotification notification = toastNotificationInstance.get();
        notification.setup(NotificationType.ERROR, I18nKeys.Notifications_exception, new NotificationAction(I18nKeys.Notifications_viewStackTrace, () -> ErrorPopup.show(message
                                                                                                                                                                                 .toString())),
                           Collections.emptyList(), throwable.getMessage());
        notifications.insertBefore(notification.getElement(), notifications.firstChild);
    }

    public void showSuccessMessage(String i18nKey, Object... notificationArgs) {
        ToastNotification notification = toastNotificationInstance.get();
        notification.setup(NotificationType.SUCCESS, i18nKey, notificationArgs);
        notifications.insertBefore(notification.getElement(), notifications.firstChild);
    }
}
