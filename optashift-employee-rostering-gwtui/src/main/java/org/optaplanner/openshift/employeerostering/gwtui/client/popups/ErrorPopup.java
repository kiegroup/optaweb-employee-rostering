package org.optaplanner.openshift.employeerostering.gwtui.client.popups;

import java.util.LinkedList;
import java.util.Queue;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.gwtbootstrap3.client.ui.html.Span;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.images.ImageResources;

public class ErrorPopup extends PopupPanel {
    final static Queue<String> queuedErrors = new LinkedList<String>();
    
    public ErrorPopup(String msg) {
        super(false);
        
        CssResources.INSTANCE.errorpopup().ensureInjected();
        setGlassEnabled(true);
        setStyleName(CssResources.INSTANCE.errorpopup().panel());
        
        VerticalPanel vertPanel = new VerticalPanel();
        HorizontalPanel horizontalSubpanel = new HorizontalPanel();
        horizontalSubpanel.add(new Image(ImageResources.INSTANCE.errorIcon()));
        horizontalSubpanel.add(new Span(new SafeHtmlBuilder()
                .appendEscapedLines(msg)
                .toSafeHtml().asString()));
        
        vertPanel.add(horizontalSubpanel);
        
        horizontalSubpanel = new HorizontalPanel();
        horizontalSubpanel.add(new Span());
        Button button = new Button("Close");
        button.addClickHandler((e) -> {
            ErrorPopup.this.hide();
            queuedErrors.poll();
            if (!queuedErrors.isEmpty()) {
                Timer timer = new Timer() {
                    @Override
                    public void run() {
                        showNextQueueError();
                    }
                };
                timer.schedule(500);
            }});
        horizontalSubpanel.add(button);
        
        vertPanel.add(horizontalSubpanel);
        setWidget(vertPanel);
    }
    
    public static void show(String msg) {
        queuedErrors.add(msg);
        if (1 == queuedErrors.size()) {
            showNextQueueError();
        }
    }
    
    private static void showNextQueueError() {
        final ErrorPopup popup = new ErrorPopup(queuedErrors.peek());
        popup.center();
    }
}
