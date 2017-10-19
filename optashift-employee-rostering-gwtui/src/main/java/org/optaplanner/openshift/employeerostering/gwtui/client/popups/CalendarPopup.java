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
import com.google.gwt.user.datepicker.client.CalendarView;
import com.google.gwt.user.datepicker.client.DefaultCalendarView;
import org.gwtbootstrap3.client.ui.html.Span;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.images.ImageResources;

public class CalendarPopup extends PopupPanel {
    
    public CalendarPopup() {
        super(false);
        
        //CssResources.INSTANCE.errorpopup().ensureInjected();
        setGlassEnabled(true);
    }
    
    public static void showCalendar() {
        CalendarPopup popup = new CalendarPopup();
        popup.show();
    }
}
