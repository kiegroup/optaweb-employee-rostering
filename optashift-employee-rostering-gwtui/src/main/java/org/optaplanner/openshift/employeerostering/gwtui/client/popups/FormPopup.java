package org.optaplanner.openshift.employeerostering.gwtui.client.popups;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.PopupPanel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;

public class FormPopup extends PopupPanel {

    private static FormPopup formPopup = null;

    private FormPopup(IsElement content) {
        super(false);

        setStyleName(getStyles().panel());
        setGlassStyleName(getStyles().glass());
        setGlassEnabled(true);

        Div container = new Div();
        container.getElement().appendChild(Element.as((JavaScriptObject) content.getElement()));
        setWidget(container);

        formPopup = this;
    }

    @Override
    public void hide() {
        super.hide();
        formPopup = null;
    }

    public static CssResources.PopupCss getStyles() {
        CssResources.INSTANCE.popup().ensureInjected();
        return CssResources.INSTANCE.popup();
    }

    public static FormPopup getFormPopup(IsElement content) {
        if (null != formPopup) {
            throw new IllegalStateException("Cannot have two form popups at once!");
        }
        return new FormPopup(content);
    }

}
