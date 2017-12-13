package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.i18n.client.LocaleInfo;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.Event;
import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;

//TODO: See if you can replace me with a Google Calendar widget
public abstract class WeekView<G extends HasTitle, I extends HasTimeslot<G>> implements CalendarPresenter<G, I> {

    Calendar<G, I> calendar;

    private static final String BACKGROUND_1 = "#efefef";
    private static final String BACKGROUND_2 = "#e0e0e0";
    private static final String BACKGROUND_ALT_1 = "#d7d7d7";
    private static final String BACKGROUND_ALT_2 = "#c7c7c7";

    private static final String LINE_COLOR = "#000000";
    private static final int HEADER_HEIGHT = 64;
    private static final String[] WEEKDAYS = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().weekdaysFull();

    double x, y;
    private Collection<I> shifts;

    public WeekView(Calendar<G, I> calendar) {
        this.calendar = calendar;
        x = 0;
        y = 0;
    }

    public void draw(CanvasRenderingContext2D g, double screenWidth, double screenHeight) {
        g.clearRect(0, 0, screenWidth, screenHeight);
        double dayWidth = screenWidth / 8.0;
        double hourHeight = (screenHeight - HEADER_HEIGHT) / 24.0;

        final int WIDTH_PADDING = 10;
        final int HEIGHT_PADDING = 10;

        for (int x = 1; x < 8; x++) {
            for (int y = 0; y < 24; y++) {
                if (1 == (x & 1)) {
                    if (0 == (y & 1)) {
                        CanvasUtils.setFillColor(g, BACKGROUND_1);
                    } else {
                        CanvasUtils.setFillColor(g, BACKGROUND_2);
                    }
                } else {
                    if (0 == (y & 1)) {
                        CanvasUtils.setFillColor(g, BACKGROUND_ALT_1);
                    } else {
                        CanvasUtils.setFillColor(g, BACKGROUND_ALT_2);
                    }
                }
                g.fillRect(x * dayWidth, HEADER_HEIGHT + y * hourHeight, dayWidth, hourHeight);
            }
        }

        int fontSize = Integer.MAX_VALUE;
        CanvasUtils.setFillColor(g, LINE_COLOR);
        for (int i = 0; i < 7; i++) {
            int fittedFontSize = CanvasUtils.fitTextToBox(g, WEEKDAYS[i], dayWidth - WIDTH_PADDING, HEADER_HEIGHT
                    - HEIGHT_PADDING);
            if (fittedFontSize < fontSize) {
                fontSize = fittedFontSize;
            }
        }

        g.font = CanvasUtils.getFont(fontSize);
        for (int i = 0; i < 7; i++) {
            CanvasUtils.drawLine(g, dayWidth * (i + 1), 0, dayWidth * (i + 1), screenHeight, 1);
            double textWidth = g.measureText(WEEKDAYS[i]).width;
            double textHeight = CanvasUtils.getTextHeight(g, fontSize);

            g.fillText(WEEKDAYS[i], dayWidth * (i + 1) + (dayWidth - textWidth) / 2, HEADER_HEIGHT - (HEADER_HEIGHT
                    - textHeight) / 2);
        }

        fontSize = CanvasUtils.fitTextToBox(g, "0:00", dayWidth, hourHeight);
        g.font = CanvasUtils.getFont(fontSize);
        for (int i = 0; i < 24; i++) {
            CanvasUtils.drawLine(g, 0, HEADER_HEIGHT + i * hourHeight, screenWidth, HEADER_HEIGHT + i * hourHeight, 1);
            g.fillText(i + ":00", dayWidth - g.measureText(i + ":00").width, HEADER_HEIGHT + (i + 1) * hourHeight);
        }

        g.fillRect(x, y, 5, 5);
    }

}
