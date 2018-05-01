package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.FormPopup;

@Templated
public class NotificationMessage implements IsElement {

    @Inject
    @DataField("title")
    @Named("span")
    private HTMLElement messageTitle;

    @Inject
    @DataField("body")
    private HTMLDivElement messageBody;

    @Inject
    @DataField("close-button")
    private HTMLButtonElement closeWindowButton;

    @Inject
    @DataField("okay-button")
    private HTMLButtonElement okayButton;

    private FormPopup formPopup;

    public NotificationMessage withTitle(String title) {
        messageTitle.innerHTML = new SafeHtmlBuilder().appendEscaped(title).toSafeHtml().asString();
        return this;
    }

    public NotificationMessage withMessage(String message) {
        messageBody.innerHTML = new SafeHtmlBuilder().appendEscaped(message).toSafeHtml().asString();
        return this;
    }

    public void show() {
        formPopup = FormPopup.getFormPopup(this);
        formPopup.center((int) JQuery.get(getElement()).width(), (int) JQuery.get(getElement()).height());
    }

    @EventHandler("okay-button")
    public void onCancelButtonClick(@ForEvent("click") final MouseEvent e) {
        formPopup.hide();
        e.stopPropagation();
    }

    @EventHandler("close-button")
    public void onCloseButtonClick(@ForEvent("click") final MouseEvent e) {
        formPopup.hide();
        e.stopPropagation();
    }

}
