/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.gwtui.client.common;

import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.optaweb.employeerostering.gwtui.client.popups.FormPopup;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;

public abstract class AbstractFormPopup implements IsElement {

    @DataField("root")
    private HTMLDivElement root;

    @DataField("popup-title")
    private HTMLElement popupTitle;

    @DataField("close-button")
    private HTMLButtonElement closeButton;

    @DataField("cancel-button")
    private HTMLButtonElement cancelButton;

    private PopupFactory popupFactory;

    private FormPopup formPopup;

    @Inject
    public AbstractFormPopup(PopupFactory popupFactory, HTMLDivElement root, @Named("span") HTMLElement popupTitle, HTMLButtonElement closeButton,
                             HTMLButtonElement cancelButton) {
        this.popupFactory = popupFactory;
        this.root = root;
        this.popupTitle = popupTitle;
        this.closeButton = closeButton;
        this.cancelButton = cancelButton;
    }

    public void show() {
        popupFactory.getFormPopup(this).ifPresent(fp -> {
            formPopup = fp;
            formPopup.show();
        });
    }

    public void showFor(IsElement element) {
        popupFactory.getFormPopup(this).ifPresent(fp -> {
            formPopup = fp;
            formPopup.showFor(element);
        });
    }

    public void hide() {
        formPopup.hide();
    }

    public void setTitle(String html) {
        popupTitle.innerHTML = html;
    }

    // Prevent other handlers behind the form from being activated
    @EventHandler("root")
    public void onClick(@ForEvent("click") final MouseEvent e) {
        e.stopPropagation();
    }

    @EventHandler("cancel-button")
    public void onCancelButtonClick(@ForEvent("click") final MouseEvent e) {
        formPopup.hide();
        e.stopPropagation();
        onClose();
    }

    @EventHandler("close-button")
    public void onCloseButtonClick(@ForEvent("click") final MouseEvent e) {
        formPopup.hide();
        e.stopPropagation();
        onClose();
    }

    protected abstract void onClose();
}
