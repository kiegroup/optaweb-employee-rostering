package org.optaplanner.openshift.employeerostering.gwtui.client.popups;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.gwtbootstrap3.client.ui.html.Span;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.images.ImageResources;

public class LoadingPopup extends PopupPanel {

    private static LoadingPopup INSTANCE;

    Span text;
    Set<String> itemLoadingSet;
    private HandlerRegistration windowResizeHandler;

    public LoadingPopup() {
        super(false);

        CssResources.INSTANCE.loadingIcon().ensureInjected();
        setGlassEnabled(true);
        setStyleName(CssResources.INSTANCE.loadingIcon().panel());

        HorizontalPanel horizontalSubpanel = new HorizontalPanel();
        Span loadingIcon = new Span();
        loadingIcon.getElement().setAttribute("class", "glyphicon glyphicon-refresh " + CssResources.INSTANCE
                .loadingIcon()
                .spin());

        horizontalSubpanel.add(loadingIcon);

        text = new Span();
        text.setHTML("Loading...");
        itemLoadingSet = new HashSet<>();

        horizontalSubpanel.add(text);

        setWidget(horizontalSubpanel);
    }

    public static void setLoading(String item) {
        if (null == INSTANCE) {
            INSTANCE = new LoadingPopup();
        }
        if (INSTANCE.itemLoadingSet.isEmpty()) {
            INSTANCE.setPopupPositionAndShow((w, h) -> {
                INSTANCE.setPopupPosition(Window.getClientWidth() / 4, Window.getClientHeight() / 4);
                INSTANCE.windowResizeHandler = Window.addResizeHandler((e) -> {
                    INSTANCE.setPopupPosition(e.getWidth() / 4, e.getHeight() / 4);
                });
            });
        }
        INSTANCE.itemLoadingSet.add(item);
    }

    public static void clearLoading(String item) {
        INSTANCE.itemLoadingSet.remove(item);
        if (INSTANCE.itemLoadingSet.isEmpty()) {
            INSTANCE.hide();
            INSTANCE.removeFromParent();
            INSTANCE.windowResizeHandler.removeHandler();
        }
    }

    public static boolean isLoading() {
        return !INSTANCE.itemLoadingSet.isEmpty();
    }
}
