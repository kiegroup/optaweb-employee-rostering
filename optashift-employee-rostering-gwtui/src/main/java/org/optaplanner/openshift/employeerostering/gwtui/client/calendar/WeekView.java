package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Collection;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;

public class WeekView implements CalendarView{
    Calendar calendar;
    
    private static final String BACKGROUND_1 = "#efefef";
    private static final String BACKGROUND_2 = "#e0e0e0";
    private static final String BACKGROUND_ALT_1 = "#d7d7d7";
    private static final String BACKGROUND_ALT_2 = "#c7c7c7";
    
    private static final String LINE_COLOR = "#000000";
    private static final int HEADER_HEIGHT = 64;
    private static final String[] WEEKDAYS = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().weekdaysFull();
    
    int x, y;
    
    public WeekView(Calendar calendar) {
        this.calendar = calendar;
        x = 0;
        y = 0;
    }
    
    @Override
    public void draw(Context2d g, int screenWidth, int screenHeight, Collection<Calendar.ShiftData> shifts) {       
        double dayWidth = screenWidth/8.0;
        double hourHeight = (screenHeight - HEADER_HEIGHT)/24.0;
        
        final int WIDTH_PADDING = 10;
        final int HEIGHT_PADDING = 10;
        
        for (int x = 1; x < 8; x++) {
            for (int y = 0; y < 24; y++) {
                if (1 == (x & 1)) {
                    if (0 == (y & 1)) {
                        g.setFillStyle(BACKGROUND_1);
                    }
                    else {
                        g.setFillStyle(BACKGROUND_2);
                    }
                }
                else {
                    if (0 == (y & 1)) {
                        g.setFillStyle(BACKGROUND_ALT_1);
                    }
                    else {
                        g.setFillStyle(BACKGROUND_ALT_2);
                    }
                }
                g.fillRect(x*dayWidth, HEADER_HEIGHT + y*hourHeight, dayWidth, hourHeight);
            }
        }
        
        int fontSize = Integer.MAX_VALUE;
        g.setFillStyle(LINE_COLOR);
        for (int i = 0; i < 7; i++) {
            int fittedFontSize = CanvasUtils.fitTextToBox(g, WEEKDAYS[i], dayWidth - WIDTH_PADDING, HEADER_HEIGHT - HEIGHT_PADDING);
            if (fittedFontSize < fontSize) {
                fontSize = fittedFontSize;
            }
        }
        
        g.setFont(CanvasUtils.getFont(fontSize));
        for (int i = 0; i < 7; i++) {
            CanvasUtils.drawLine(g, dayWidth*(i+1), 0, dayWidth*(i+1), screenHeight);
            double textWidth = g.measureText(WEEKDAYS[i]).getWidth();
            double textHeight = CanvasUtils.getTextHeight(g, fontSize);
            
            g.fillText(WEEKDAYS[i], dayWidth*(i+1) + (dayWidth - textWidth)/2, HEADER_HEIGHT - (HEADER_HEIGHT - textHeight)/2);
        }
        
        fontSize = CanvasUtils.fitTextToBox(g, "0:00", dayWidth, hourHeight);
        g.setFont(CanvasUtils.getFont(fontSize));
        for (int i = 0; i < 24; i++) {
            CanvasUtils.drawLine(g, 0, HEADER_HEIGHT + i*hourHeight, screenWidth, HEADER_HEIGHT + i*hourHeight);
            g.fillText(i + ":00",dayWidth - g.measureText(i + ":00").getWidth(), HEADER_HEIGHT + (i+1)*hourHeight);
        }
        g.setFillStyle("#000000");
        g.fillRect(x, y, 30, 30);
    }
    
    @Override
    public void onMouseDown(MouseDownEvent e) {
        x = e.getX();
        y = e.getY();
        calendar.draw();
    }
    
    @Override
    public void onMouseUp(MouseUpEvent e) {
        x = e.getX();
        y = e.getY();
        calendar.draw();
    }

    @Override
    public void onMouseMove(MouseMoveEvent e) {
        x = e.getX();
        y = e.getY();
        calendar.draw();
    }
}
