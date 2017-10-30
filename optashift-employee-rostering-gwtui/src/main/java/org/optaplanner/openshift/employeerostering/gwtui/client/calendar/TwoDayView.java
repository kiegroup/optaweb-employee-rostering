package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.awt.geom.Point2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.ListDataProvider;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

public class TwoDayView implements CalendarView {
    Calendar calendar;
    
    private static final String BACKGROUND_1 = "#efefef";
    private static final String BACKGROUND_2 = "#e0e0e0";
    private static final String LINE_COLOR = "#000000";
    private static final String[] WEEKDAYS = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().weekdaysFull();
    
    private static final int HEADER_HEIGHT = 64;
    private static final double SPOT_NAME_WIDTH = 200;
    
    
    private List<Spot> spots = new ArrayList<>();
    private HashMap<String,Long> spotPos = new HashMap<>();
    private HashMap<String, DynamicContainer> spotContainer = new HashMap<>();
    private Collection<ShiftData> shifts;
    private Collection<ShiftDrawable> shiftDrawables;
    LocalDateTime curr;
    
    double mouseX, mouseY;
    double widthPerMinute, spotHeight;
    long totalSpotSlots; 
    String popupText;
    
    public TwoDayView(Calendar calendar) {
        this.calendar = calendar;
        curr = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        SpotRestServiceBuilder.getSpotList(calendar.getTenantId(), new FailureShownRestCallback<List<Spot>>() {
            @Override
            public void onSuccess(List<Spot> spotList) {
                spots = spotList;
                calendar.draw();
            }
        });
        mouseX = 0;
        mouseY = 0;
        popupText = null;
    }
    
    @Override
    public void draw(CanvasRenderingContext2D g, double screenWidth, double screenHeight) {
        g.clearRect(0, 0, screenWidth, screenHeight);
        long secondsPerDay = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
                .plusDays(1).toEpochSecond(ZoneOffset.UTC);
        long minutesPerDay = secondsPerDay/60;
        widthPerMinute = (screenWidth - SPOT_NAME_WIDTH)/(2*minutesPerDay);
        spotHeight = (screenHeight - HEADER_HEIGHT)/totalSpotSlots;
        
        drawShiftsBackground(g, screenWidth, screenHeight);
        drawSpots(g, screenWidth, screenHeight);
        drawTimes(g, screenWidth, screenHeight);
        drawPopup(g, screenWidth, screenHeight);
    }
    
    private void drawSpots(CanvasRenderingContext2D g, double screenWidth, double screenHeight) {
        int minSize = Integer.MAX_VALUE;
        for (Spot spot : spots) {
            minSize = Math.min(minSize, CanvasUtils.fitTextToBox(g, spot.getName(), SPOT_NAME_WIDTH, spotHeight));
        }
        g.font = CanvasUtils.getFont(minSize);
        
        CanvasUtils.setFillColor(g, "#000000");
        double textHeight = CanvasUtils.getTextHeight(g, minSize);
        for (Spot spot : spots) {
            long pos = spotPos.get(spot.getName());
            g.fillText(spot.getName(), 0, HEADER_HEIGHT + spotHeight*pos + textHeight + (spotHeight - textHeight)/2);
            CanvasUtils.drawLine(g, 0, HEADER_HEIGHT + spotHeight*pos, screenWidth, HEADER_HEIGHT + spotHeight*pos);
        }
        
        for (ShiftDrawable drawable : shiftDrawables) {
            drawable.draw(g);
        }
    }
    
    private void drawPopup(CanvasRenderingContext2D g, double screenWidth, double screenHeight) {
        if (null != popupText) {
            g.font = CanvasUtils.getFont(12);
            double[] preferredSize = CanvasUtils.getPreferredBoxSizeForText(g, popupText, 12);
            g.strokeRect(getMouseX() - preferredSize[0], getMouseY() - preferredSize[1], preferredSize[0], preferredSize[1]);
            CanvasUtils.setFillColor(g, "#B18800");
            g.fillRect(getMouseX() - preferredSize[0], getMouseY() - preferredSize[1], preferredSize[0], preferredSize[1]);
            CanvasUtils.setFillColor(g, "#000000");
            CanvasUtils.drawTextInBox(g, popupText, getMouseX() - preferredSize[0], getMouseY() - preferredSize[1], preferredSize[0], preferredSize[1]);
            popupText = null;
        }
    }
    
    private void drawShiftsBackground(CanvasRenderingContext2D g, double screenWidth, double screenHeight) {
        for (int x = 0; x < 48; x++) {
            if (x % 2 == 0) {
                CanvasUtils.setFillColor(g, BACKGROUND_1);
            }
            else {
                CanvasUtils.setFillColor(g, BACKGROUND_2);
            }
            g.fillRect(SPOT_NAME_WIDTH + x*widthPerMinute*60, HEADER_HEIGHT, SPOT_NAME_WIDTH + (x+1)*widthPerMinute*60, screenHeight - HEADER_HEIGHT);
        }
    }
    
    private void drawTimes(CanvasRenderingContext2D g, double screenWidth, double screenHeight) {
        CanvasUtils.setFillColor(g, "#000000");
        
        
        for (int x = 0; x < 8; x++) {
            g.fillText(((6*x) % 24) + ":00", SPOT_NAME_WIDTH + x*6*60*widthPerMinute, HEADER_HEIGHT);
            CanvasUtils.drawLine(g, SPOT_NAME_WIDTH + x*6*widthPerMinute*60, HEADER_HEIGHT, SPOT_NAME_WIDTH + x*6*widthPerMinute*60, screenHeight);
        }
    }
    
    @Override
    public void onMouseDown(MouseEvent e) {
        mouseX = CanvasUtils.getCanvasX(calendar.canvas, e);
        mouseY = CanvasUtils.getCanvasY(calendar.canvas, e);
        calendar.draw();
    }
    
    @Override
    public void onMouseUp(MouseEvent e) {
        mouseX = CanvasUtils.getCanvasX(calendar.canvas, e);
        mouseY = CanvasUtils.getCanvasY(calendar.canvas, e);
        calendar.draw();
    }

    @Override
    public void onMouseMove(MouseEvent e) {
        mouseX = CanvasUtils.getCanvasX(calendar.canvas, e);
        mouseY = CanvasUtils.getCanvasY(calendar.canvas, e);
        calendar.draw();
    }
    
    private List<ShiftData> getShiftsDuring(LocalDateTime time, Collection<ShiftData> shifts) {
        return shifts.stream().filter((shift) -> isTimeInShift(time, shift)).collect(Collectors.toList());
    }
    
    private static boolean isTimeInShift(LocalDateTime time, ShiftData shift) {
        return !shift.start.isAfter(time) && shift.end.isAfter(time);
    }

    @Override
    public void setShifts(Collection<ShiftData> shifts) {
        this.shifts = shifts;
        shiftDrawables = new ArrayList<>();
        totalSpotSlots = 0;
        spotPos.clear();
        spotContainer.clear();
        HashMap<String, HashMap<ShiftData,Integer>> placedSpots = new HashMap<>();
        HashMap<String, String> colorMap = new HashMap<>();
        
        for (Spot spot : spots.stream().sorted((a,b) -> stringWithIntCompareTo(a.getName(),b.getName())).collect(Collectors.toList())) {
            spotPos.put(spot.getName(), totalSpotSlots);
            final long spotStartPos = totalSpotSlots;
            spotContainer.put(spot.getName(), new DynamicContainer(()->new Position(SPOT_NAME_WIDTH, HEADER_HEIGHT + spotStartPos*getSpotHeight())));
            colorMap.put(spot.getName(), ColorUtils.getColor(colorMap.size()));
            long max = 1;
            List<ShiftData> spotShifts = shifts.stream().filter((shift) -> shift.spots.contains(spot.getName())).collect(Collectors.toList());
            HashMap<ShiftData,Integer> placedShifts = new HashMap<ShiftData,Integer>();
            for (ShiftData spotShift : spotShifts) {
                List<ShiftData> concurrentShifts = getShiftsDuring(spotShift.start, placedShifts.keySet());
                HashMap<ShiftData,Integer> concurrentPlacedShifts = new HashMap<>();
                placedShifts.forEach((k,v) -> {
                    if (concurrentShifts.contains(k)) {
                        concurrentPlacedShifts.put(k, v);
;                    }
                });
                int index = 0;
                while (concurrentPlacedShifts.containsValue(index)) {
                    index++;
                }
                placedShifts.put(spotShift, index);
                max = Math.max(max, index);
            }
            totalSpotSlots += max;
            placedSpots.put(spot.getName(), placedShifts);
        }
        
        for (ShiftData shift : shifts) {
            for (String spot : shift.spots) {
                if (placedSpots.containsKey(spot) && placedSpots.get(spot).containsKey(shift)) {
                    ShiftDrawable drawable = new ShiftDrawable(this,
                            spot,
                            shift.start, shift.end,
                            colorMap.get(spot),
                            placedSpots.get(spot).get(shift));
                    drawable.setParent(spotContainer.get(spot));
                    shiftDrawables.add(drawable);
                }
            }
        }
    }
    
    public double getWidthPerMinute() {
        return widthPerMinute;
    }
    
    public double getSpotHeight() {
        return spotHeight;
    }
    
    public double getMouseX() {
        return mouseX;
    }
    
    public double getMouseY() {
        return mouseY;
    }
    
    
    //TODO: Move this to another file
    //This is a lexicographic ordering of strings, but for different strings
    //of the format s(num1)p and s(num2)q, it would use numeric ordering on the num1 and num2 part (so 10 > 2)
    private static int stringWithIntCompareTo(String a, String b) {
        Iterator<Integer> aIter = a.chars().iterator();
        Iterator<Integer> bIter = b.chars().iterator();
        
        while (true) {
            if (!aIter.hasNext() && !bIter.hasNext()) {
                return 0;
            }
            else if (!aIter.hasNext()) {
                return -1;
            }
            else if (!bIter.hasNext()) {
                return 1;
            }
            
            int aChar = aIter.next();
            int bChar = bIter.next();
            
            if (isDigit(aChar)) {
                if (isDigit(bChar)) {
                    char[] aValue = Character.toChars(aChar);
                    char[] bValue = Character.toChars(bChar);
                    StringBuilder aNum = new StringBuilder();
                    StringBuilder bNum = new StringBuilder();
                    boolean aHasChar = false;
                    boolean bHasChar = false;
                    
                    for (char c : aValue) {
                        aNum.append(c);
                    }
                    for (char c : bValue) {
                        bNum.append(c);
                    }
                    
                    while (aIter.hasNext()) {
                        aChar = aIter.next();
                        aValue = Character.toChars(aChar);
                        if (!isDigit(aChar)) {
                            aHasChar = true;
                            break;
                        }
                        for (char c : aValue) {
                            aNum.append(c);
                        }
                    }
                    while (bIter.hasNext()) {
                        bChar = bIter.next();
                        bValue = Character.toChars(bChar);
                        if (!isDigit(aChar)) {
                            bHasChar = true;
                            break;
                        }
                        for (char c : aValue) {
                            bNum.append(c);
                        }
                    }
                    int aInt = Integer.parseInt(aNum.toString());
                    int bInt = Integer.parseInt(bNum.toString());
                    if (aInt != bInt) {
                        return Integer.compare(aInt, bInt);
                    }
                    if (aHasChar && bHasChar) {
                        if (Integer.compare(aChar, bChar) != 0) {
                            return Integer.compare(aChar, bChar);
                        }
                    }
                    else if (aHasChar) {
                        return 1;
                    }
                    else if (bHasChar) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                    
                }
                else {
                    return Integer.compare(aChar, bChar);
                }
            }
            else {
                if (Integer.compare(aChar, bChar) != 0) {
                    return Integer.compare(aChar, bChar);
                }
            }
        }
    }
    
    private static boolean isDigit(int a) {
        char[] chars = Character.toChars(a);
        return chars.length == 1 && Character.isDigit(chars[0]);
    }

    public void preparePopup(String text) {
        popupText = text;
    }

}
