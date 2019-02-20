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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.user.client.Timer;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLUListElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public class ToastNotification
        implements
        IsElement {

    @Inject
    @DataField("close-notification-button")
    private HTMLButtonElement closeNotificationButton;

    @Inject
    @DataField("show-dropdown-actions-button")
    private HTMLButtonElement showDropdownActionsButton;

    @Inject
    @DataField("dropdown-actions")
    private HTMLUListElement dropdownActions;

    @Inject
    @DataField("notification-actions")
    private HTMLDivElement notificationActions;

    @Inject
    @DataField("notification-icon")
    @Named("span")
    private HTMLElement notificationIcon;

    @Inject
    @DataField("notification-message")
    @Named("span")
    private HTMLElement notificationMessage;

    @Inject
    private HTMLAnchorElement anchorElementFactory;

    @Inject
    private TranslationService translationService;

    private Timer autoDismissTimer;

    @PostConstruct
    public void init() {
        autoDismissTimer = new Timer() {

            @Override
            public void run() {
                getElement().classList.add("fade-and-slide-out");
                new Timer() {

                    @Override
                    public void run() {
                        getElement().remove();
                    }

                }.schedule(2000);
            }

        };

        autoDismissTimer.schedule(10000);
    }

    @EventHandler("toast-notification")
    public void onMouseEnter(@ForEvent("mouseenter") MouseEvent e) {
        autoDismissTimer.cancel();
    }

    public void setup(NotificationType notificationType, String i18nKey, Object... notificationArgs) {
        setup(notificationType, i18nKey, null, Collections.emptyList(), notificationArgs);
    }

    public void setup(NotificationType notificationType, String i18nKey, NotificationAction action, List<NotificationAction> dropdownActions, Object... notificationArgs) {
        getElement().classList.add(notificationType.getNotificationTypeClass());
        notificationIcon.classList.add(notificationType.getNotificationIconClass());
        notificationMessage.innerHTML = translationService.format(i18nKey, notificationArgs);
        if (dropdownActions.isEmpty()) {
            this.showDropdownActionsButton.classList.add("hidden");
        } else {
            for (NotificationAction dropdownAction : dropdownActions) {
                HTMLAnchorElement anchor = (HTMLAnchorElement) (anchorElementFactory.cloneNode(false));
                anchor.innerHTML = translationService.format(dropdownAction.getI18nKey());
                anchor.addEventListener("click", e -> dropdownAction.performAction());
                notificationActions.appendChild(anchor);
            }
        }

        if (null == action) {
            this.notificationActions.classList.add("hidden");
        } else {
            HTMLAnchorElement anchor = (HTMLAnchorElement) (anchorElementFactory.cloneNode(false));
            anchor.innerHTML = translationService.format(action.getI18nKey());
            anchor.addEventListener("click", e -> action.performAction());
            notificationActions.appendChild(anchor);
        }
    }

}
