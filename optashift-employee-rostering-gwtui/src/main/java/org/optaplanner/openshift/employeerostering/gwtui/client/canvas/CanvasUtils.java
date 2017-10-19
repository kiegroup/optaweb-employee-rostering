package org.optaplanner.openshift.employeerostering.gwtui.client.canvas;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.TextBaseline;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.RootPanel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;

public class CanvasUtils {
    public static String FONT_FAMILY = "Arial";
    
    public static void drawLine(Context2d g, double x1, double y1, double x2, double y2) {
        g.beginPath();
        g.moveTo(x1, y1);
        g.lineTo(x2, y2);
        g.closePath();
        g.stroke();
    }
    
    public static String getFont(int size) {
        return size + "px " + FONT_FAMILY;
    }
    
    public static int fitTextToBox(Context2d g, String text, double w, double h) {
        String oldFont = g.getFont();
        String oldBaseline = g.getTextBaseline();
        
        int size = 1;
        TextMetrics m;
        
        g.setTextBaseline(TextBaseline.TOP);
        do {
            size++;
            g.setFont(getFont(size));
            m = g.measureText(text);
        }
        while (m.getWidth() < w);
        size--;
        
        while (size > 1 && getTextHeight(g, size) > h) {
            size--;
        }
        
        g.setFont(oldFont);
        g.setTextBaseline(oldBaseline);
        return size;
    }
    
    public static double getTextHeight(Context2d g, int size) {
        String oldFont = g.getFont();
        g.setFont(getFont(size));
        double out = g.measureText("M").getWidth();
        g.setFont(oldFont);
        return out;
    }
}
