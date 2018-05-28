package org.optaplanner.openshift.employeerostering.gwtui.client.popups;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;
import org.gwtbootstrap3.client.ui.html.Div;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.JQuery;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;

public class FormPopup extends PopupPanel {

    protected FormPopup(IsElement content) {
        super(false);

        setStyleName(getStyles().panel());
        setGlassStyleName(getStyles().glass());
        setGlassEnabled(true);

        Div container = new Div();
        container.getElement().appendChild(Js.cast(content.getElement()));
        setWidget(container);
    }

    public void showFor(IsElement isElement) {
        final HTMLElement element = isElement.getElement();
        setPopupPositionAndShow((w, h) -> {
            final Integer offsetLeft = (int) Math.round(getOffsetRight(element));
            final Integer offsetTop = (int) Math.round(getOffsetTop(element));
            setPopupPosition(offsetLeft, offsetTop);
        });
    }

    public void center(final int width, final int height) {
        this.getContainerElement().getStyle().setPosition(Position.FIXED);
        this.setPopupPositionAndShow((w, h) -> {
            this.setPopupPosition(Window.getClientWidth() / 2 - width / 2,
                    Window.getClientHeight() / 2 - height / 2);
        });

    }

    private double getOffsetRight(HTMLElement element) {
        return JQuery.get(element).offset().left + element.scrollWidth;
    }

    private double getOffsetTop(HTMLElement element) {
        return JQuery.get(element).offset().top;
    }

    @Override
    public void hide() {
        super.hide();
    }

    public static CssResources.PopupCss getStyles() {
        CssResources.INSTANCE.popup().ensureInjected();
        return CssResources.INSTANCE.popup();
    }

}
