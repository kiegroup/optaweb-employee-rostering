package org.optaplanner.openshift.employeerostering.gwtui.client.popups;

import com.google.gwt.user.client.ui.PopupPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;

public class FormPopup extends PopupPanel {

    private static FormPopup formPopup = null;

    private FormPopup() {
        super(false);

        setStyleName(getStyles().panel());
        setGlassStyleName(getStyles().glass());
        setGlassEnabled(true);

        formPopup = this;
    }

    @Override
    public void show() {
        super.show();
        getContainerElement().setClassName(getStyles().form());
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

    public static FormPopup getFormPopup() {
        if (null != formPopup) {
            throw new IllegalStateException("Cannot have two form popups at once!");
        }
        return new FormPopup();
    }

}
