package org.optaplanner.openshift.employeerostering.gwtui.client.popups;

import java.util.LinkedList;
import java.util.Queue;

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

public class ErrorPopup extends PopupPanel {

    final static Queue<String> errorQueue = new LinkedList<String>();

    private HandlerRegistration windowResizeHandler;

    public ErrorPopup(String msg) {
        super(false);

        CssResources.INSTANCE.errorpopup().ensureInjected();
        setStyleName(CssResources.INSTANCE.errorpopup().panel());
        setGlassStyleName(CssResources.INSTANCE.errorpopup().glass());
        setGlassEnabled(true);

        VerticalPanel vertPanel = new VerticalPanel();
        HorizontalPanel horizontalSubpanel = new HorizontalPanel();
        Image image = new Image(ImageResources.INSTANCE.errorIcon());
        horizontalSubpanel.add(image);

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.setHeight(image.getHeight() + "px");
        Span content = new Span(new SafeHtmlBuilder()
                .appendEscapedLines(msg)
                .toSafeHtml().asString());

        scrollPanel.add(content);
        scrollPanel.setWidth(Window.getClientWidth() / 2 + "px");
        windowResizeHandler = Window.addResizeHandler((e) -> {
            scrollPanel.setWidth(e.getWidth() / 2 + "px");
        });

        horizontalSubpanel.add(scrollPanel);
        vertPanel.add(horizontalSubpanel);

        horizontalSubpanel = new HorizontalPanel();
        horizontalSubpanel.add(new Span());
        Button button = new Button("Close");
        button.addClickHandler((e) -> {
            ErrorPopup.this.hide();
            ErrorPopup.this.windowResizeHandler.removeHandler();
            errorQueue.poll();
            if (!errorQueue.isEmpty()) {
                Timer timer = new Timer() {

                    @Override
                    public void run() {
                        showNextQueueError();
                    }
                };
                timer.schedule(500);
            }
        });
        horizontalSubpanel.add(button);

        vertPanel.add(horizontalSubpanel);
        setWidget(vertPanel);
    }

    public static void show(String msg) {
        errorQueue.add(msg);
        if (1 == errorQueue.size()) {
            showNextQueueError();
        }
    }

    private static void showNextQueueError() {
        final ErrorPopup popup = new ErrorPopup(errorQueue.peek());
        popup.center();
    }
}
