package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.awt.geom.Point2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;
import org.gwtbootstrap3.client.ui.Pagination;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

public class TwoDayView implements CalendarView, HasRows, HasData<Collection<ShiftDrawable>> {
    Calendar calendar;
    
    private static final String BACKGROUND_1 = "#efefef";
    private static final String BACKGROUND_2 = "#e0e0e0";
    private static final String LINE_COLOR = "#000000";
    private static final String[] WEEKDAYS = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().weekdaysFull();
    private static final int WEEK_START = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().firstDayOfTheWeek();
    
    private static final int HEADER_HEIGHT = 64;
    private static final double SPOT_NAME_WIDTH = 200;
    
    
    private List<Spot> spots = new ArrayList<>();
    private Collection<Handler> rangeHandlers = new ArrayList<>();
    private Collection<com.google.gwt.view.client.RowCountChangeEvent.Handler> rowCountHandlers = new ArrayList<>();
    private HashMap<String,Integer> spotPos = new HashMap<>();
    private HashMap<String,Integer> cursorIndex = new HashMap<>();
    private HashMap<String, DynamicContainer> spotContainer = new HashMap<>();
    private HashMap<String, DynamicContainer> spotAddPlane = new HashMap<>();
    private Collection<ShiftData> shifts;
    private Collection<ShiftDrawable> shiftDrawables;
    private Pagination pagination;
    private SimplePager pager;
    private ListDataProvider<Collection<ShiftDrawable>> dataProvider = new ListDataProvider<>();
    private SelectionModel<? super Collection<ShiftDrawable>> selectionModel;
    private int rangeStart, rangeEnd;
    private int totalDisplayedSpotSlots;
    int currDay;
    
    double mouseX, mouseY;
    double dragStartX, dragStartY;
    double widthPerMinute, spotHeight;
    int totalSpotSlots;
    boolean isDragging, creatingEvent;
    String selectedSpot;
    Long selectedIndex;
    String overSpot;
    String popupText;
    
    Panel topPanel, bottomPanel, sidePanel;
    
    public TwoDayView(Calendar calendar, Panel top, Panel bottom, Panel side) {
        this.calendar = calendar;
        currDay = 0;
        mouseX = 0;
        mouseY = 0;
        rangeStart = 0;
        rangeEnd = 10;
        totalDisplayedSpotSlots = 10;
        selectedSpot = null;
        isDragging = false;
        creatingEvent = false;
        popupText = null;
        selectedIndex = null;
        topPanel = top;
        bottomPanel = bottom;
        sidePanel = side;
        shiftDrawables = new ArrayList<>();
        selectionModel = new NoSelectionModel<Collection<ShiftDrawable>>((g) -> (g.isEmpty())? null : g.iterator().next().getGroupId());
        initPanels();
    }
    
    private void initPanels() {
        Label title = new Label();
        title.setText("Configuration Editor");
        topPanel.add(title);
        
        
        Button prevButton = new Button();
        prevButton.setText("Previous Day");
        prevButton.addClickHandler((e) -> {currDay -= 1; calendar.draw();});
        bottomPanel.add(prevButton);
        
        Button nextButton = new Button();
        nextButton.setText("Next Day");
        nextButton.addClickHandler((e) -> {currDay += 1; calendar.draw();});
        bottomPanel.add(nextButton);
        
        pagination = new Pagination();
        pager = new SimplePager();
        
        bottomPanel.add(pagination);
        
        pager.setDisplay(this);
        pager.setPageSize(totalDisplayedSpotSlots);
        pagination.clear();
        dataProvider.addDataDisplay(this);
    }
    
    @Override
    public void draw(CanvasRenderingContext2D g, double screenWidth, double screenHeight) {
        g.clearRect(0, 0, screenWidth, screenHeight);
        long secondsPerDay = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
                .plusDays(1).toEpochSecond(ZoneOffset.UTC);
        long minutesPerDay = secondsPerDay/60;
        widthPerMinute = (screenWidth - SPOT_NAME_WIDTH)/(2*minutesPerDay);
        spotHeight = (screenHeight - HEADER_HEIGHT)/totalDisplayedSpotSlots;
        
        drawShiftsBackground(g, screenWidth, screenHeight);
        drawSpots(g, screenWidth, screenHeight);
        drawTimes(g, screenWidth, screenHeight);
        drawCreateShiftForSpotBar(g, screenWidth, screenHeight);
        drawSpotToCreate(g, screenWidth, screenHeight);
        drawPopup(g, screenWidth, screenHeight);
    }
    
    private void drawSpots(CanvasRenderingContext2D g, double screenWidth, double screenHeight) {
        int minSize = Integer.MAX_VALUE;
        for (Spot spot : spots) {
            minSize = Math.min(minSize, CanvasUtils.fitTextToBox(g, spot.getName(), SPOT_NAME_WIDTH, spotHeight));
        }
        
        g.save();
        g.translate(-currDay*(60*24)*widthPerMinute, 0);
        int index = 0;
        Iterable<Collection<ShiftDrawable>> toDraw = getVisibleItems();
        Set<String> drawnSpots = new HashSet<>();
        HashMap<String, Integer> spotIndex = new HashMap<>();
        String lastGroup = null;
        for (Collection<ShiftDrawable> group : toDraw) {
            if (!group.isEmpty()) {
                String groupId = group.iterator().next().getGroupId();
                if (lastGroup != null && !lastGroup.equals(groupId)) {
                    index++;
                }
                
                if (!drawnSpots.contains(groupId)) {
                    drawnSpots.add(groupId);
                    spotIndex.put(groupId, index);
                }
                lastGroup = groupId;
            }
            for (ShiftDrawable drawable : group) {
                drawable.doDrawAt(g, drawable.getGlobalX(), HEADER_HEIGHT + index*spotHeight);
            }
            index++;
        }
        g.restore();
        
        CanvasUtils.setFillColor(g, "#FFFFFF");
        g.fillRect(0, HEADER_HEIGHT, SPOT_NAME_WIDTH, screenHeight - HEADER_HEIGHT);
        CanvasUtils.setFillColor(g, "#000000");
        double textHeight = CanvasUtils.getTextHeight(g, minSize);
        g.font = CanvasUtils.getFont(minSize);
        
        for (Spot spot : spots.stream().filter((s) -> drawnSpots.contains(s.getName())).collect(Collectors.toList())) {
            int pos = spotIndex.get(spot.getName());
            g.fillText(spot.getName(), 0, HEADER_HEIGHT + spotHeight*pos + textHeight + (spotHeight - textHeight)/2);
            CanvasUtils.drawLine(g, 0, HEADER_HEIGHT + spotHeight*pos, screenWidth, HEADER_HEIGHT + spotHeight*pos);
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
    
    private void drawSpotToCreate(CanvasRenderingContext2D g, double screenWidth, double screenHeight) {
        if (null != selectedSpot) {
            CanvasUtils.setFillColor(g, "#00FF00");
            long fromMins = Math.round((dragStartX - SPOT_NAME_WIDTH) / widthPerMinute); 
            LocalDateTime from = LocalDateTime.ofEpochSecond(60*fromMins, 0, ZoneOffset.UTC).plusDays(currDay);
            long toMins = Math.max(0,Math.round((mouseX - SPOT_NAME_WIDTH) / widthPerMinute)); 
            LocalDateTime to = LocalDateTime.ofEpochSecond(60*toMins, 0, ZoneOffset.UTC).plusDays(currDay);
            if (to.isBefore(from)) {
                LocalDateTime tmp = to;
                to = from;
                from = tmp;
            }
            StringBuilder timeslot = new StringBuilder(".");
            timeslot.append(' ');
            timeslot.append(CommonUtils.pad(from.getHour() + "", 2));
            timeslot.append(':');
            timeslot.append(CommonUtils.pad(from.getMinute() + "", 2));
            timeslot.append('-');
            timeslot.append(CommonUtils.pad(to.getHour() + "", 2));
            timeslot.append(':');
            timeslot.append(CommonUtils.pad(to.getMinute() + "", 2));
            preparePopup(timeslot.toString());
            g.fillRect(dragStartX, spotContainer.get(selectedSpot).getGlobalY() + spotHeight*selectedIndex, (toMins - fromMins)*widthPerMinute, spotHeight);
        }
    }
    
    private void drawCreateShiftForSpotBar(CanvasRenderingContext2D g, double screenWidth, double screenHeight) {
        /*if (null != selectedSpot) {
            return;
        }
        
        for (String spot : spotAddPlane.keySet()) {
            if (spotContainer.get(spot).getGlobalX() < mouseX && spotContainer.get(spot).getGlobalY() < mouseY && mouseY < spotAddPlane.get(spot).getGlobalY() + spotHeight ) {
                long index = (long) Math.floor((mouseY - spotContainer.get(spot).getGlobalY())/spotHeight);
                CanvasUtils.setFillColor(g, "#00ff00");
                if (null != overSpot) {
                    cursorIndex.put(overSpot, spotPos.get(overSpot));
                }
                overSpot = spot;
                cursorIndex.put(overSpot, index);
                g.fillRect(SPOT_NAME_WIDTH, spotContainer.get(spot).getGlobalY() + spotHeight*index, screenWidth - SPOT_NAME_WIDTH, spotHeight);
                return;
            }
        }
        if (null != overSpot) {
            cursorIndex.put(overSpot, spotPos.get(overSpot));
        }*/
    }
    
    private void handleMouseDown(double eventX, double eventY) {
        for (String spot : spotAddPlane.keySet()) {
            if (spotContainer.get(spot).getGlobalX() < mouseX && spotContainer.get(spot).getGlobalY() < mouseY && mouseY < spotAddPlane.get(spot).getGlobalY() + spotHeight ) {
                int index = (int) Math.floor((mouseY - spotContainer.get(spot).getGlobalY())/spotHeight);
                if (null != overSpot) {
                    cursorIndex.put(overSpot, spotPos.get(overSpot));
                }
                selectedSpot = spot;
                overSpot = spot;
                cursorIndex.put(overSpot, index);
                selectedIndex = (long) Math.floor((mouseY - spotContainer.get(spot).getGlobalY())/spotHeight);
                break;
            }
        }
    }
    
    private void handleMouseUp(double eventX, double eventY) {
        if (null != selectedSpot) {
            long fromMins = Math.round((dragStartX - SPOT_NAME_WIDTH) / widthPerMinute); 
            LocalDateTime from = LocalDateTime.ofEpochSecond(60*fromMins, 0, ZoneOffset.UTC).plusDays(currDay);
            long toMins = Math.max(0,Math.round((eventX - SPOT_NAME_WIDTH) / widthPerMinute)); 
            LocalDateTime to = LocalDateTime.ofEpochSecond(60*toMins, 0, ZoneOffset.UTC).plusDays(currDay);
            if (to.isBefore(from)) {
                LocalDateTime tmp = to;
                to = from;
                from = tmp;
            }
            calendar.addShift(new ShiftData(from, to, Arrays.asList(selectedSpot)));
        }
    }
    
    private void drawTimes(CanvasRenderingContext2D g, double screenWidth, double screenHeight) {
        CanvasUtils.setFillColor(g, "#000000");
        String week = "Week " + currDay/7;
        int textSize = CanvasUtils.fitTextToBox(g, week, SPOT_NAME_WIDTH, HEADER_HEIGHT/2);
        g.font = CanvasUtils.getFont(textSize);
        g.fillText(week, 0, HEADER_HEIGHT/2);
        for (int x = 0; x < 2; x++) {
            g.fillText(WEEKDAYS[Math.abs((WEEK_START + x + currDay)) % 7], SPOT_NAME_WIDTH + (24*x)*60*widthPerMinute, HEADER_HEIGHT/2);
        }
        for (int x = 0; x < 8; x++) {
            g.fillText(((6*x) % 24) + ":00", SPOT_NAME_WIDTH + x*6*60*widthPerMinute, HEADER_HEIGHT);
            CanvasUtils.drawLine(g, SPOT_NAME_WIDTH + x*6*widthPerMinute*60, HEADER_HEIGHT, SPOT_NAME_WIDTH + x*6*widthPerMinute*60, screenHeight);
        }
    }
    
    @Override
    public void onMouseDown(MouseEvent e) {
        mouseX = CanvasUtils.getCanvasX(calendar.canvas, e);
        mouseY = CanvasUtils.getCanvasY(calendar.canvas, e);
        dragStartX = mouseX;
        dragStartY = mouseY;
        handleMouseDown(mouseX, mouseY);
        isDragging = true;
        calendar.draw();
    }
    
    @Override
    public void onMouseUp(MouseEvent e) {
        mouseX = CanvasUtils.getCanvasX(calendar.canvas, e);
        mouseY = CanvasUtils.getCanvasY(calendar.canvas, e);
        handleMouseUp(mouseX, mouseY);
        isDragging = false;
        selectedSpot = null;
        calendar.draw();
    }

    @Override
    public void onMouseMove(MouseEvent e) {
        mouseX = CanvasUtils.getCanvasX(calendar.canvas, e);
        mouseY = CanvasUtils.getCanvasY(calendar.canvas, e);
        if (isDragging) {
            onMouseDrag(mouseX, mouseY);
        }
        calendar.draw();
    }
    
    private void onMouseDrag(double x, double y) {
    }
    
    private List<ShiftData> getShiftsDuring(ShiftData time, Collection<ShiftData> shifts) {
        return shifts.stream().filter((shift) -> doTimeslotsIntersect(time.start, time.end,shift.start,shift.end)).collect(Collectors.toList());
    }
    
    private static boolean doTimeslotsIntersect(LocalDateTime start1, LocalDateTime end1,
            LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    @Override
    public void setShifts(Collection<ShiftData> shifts) {
        this.shifts = shifts;
        shiftDrawables = new ArrayList<>();
        totalSpotSlots = 0;
        spotPos.clear();
        spotContainer.clear();
        spotAddPlane.clear();
        cursorIndex.clear();
        HashMap<String, HashMap<ShiftData,Integer>> placedSpots = new HashMap<>();
        HashMap<String, String> colorMap = new HashMap<>();
        
        for (Spot spot : spots.stream().sorted((a,b) -> CommonUtils.stringWithIntCompareTo(a.getName(),b.getName())).collect(Collectors.toList())) {
            spotPos.put(spot.getName(), totalSpotSlots);
            final long spotStartPos = totalSpotSlots;
            spotContainer.put(spot.getName(), new DynamicContainer(()->new Position(SPOT_NAME_WIDTH, HEADER_HEIGHT + spotStartPos*getSpotHeight())));
            colorMap.put(spot.getName(), ColorUtils.getColor(colorMap.size()));
            long max = 0;
            List<ShiftData> spotShifts = shifts.stream().filter((shift) -> shift.spots.contains(spot.getName())).collect(Collectors.toList());
            HashMap<ShiftData,Integer> placedShifts = new HashMap<ShiftData,Integer>();
            for (ShiftData spotShift : spotShifts) {
                List<ShiftData> concurrentShifts = getShiftsDuring(spotShift, placedShifts.keySet());
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
            totalSpotSlots += max + 2;
            final long spotEndPos = totalSpotSlots;
            spotAddPlane.put(spot.getName(), new DynamicContainer(() -> new Position(SPOT_NAME_WIDTH,HEADER_HEIGHT + getSpotHeight()*(spotEndPos-1))));
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
        
        for (Spot spot : spots) {
            cursorIndex.put(spot.getName(), spotPos.get(spot.getName()));
        }
        
        dataProvider.setList(getItems());
        dataProvider.flush();
        pagination.rebuild(pager);
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
    
    public double getDragStartX() {
        return dragStartX;
    }
    
    public double getDragStartY() {
        return dragStartY;
    }
    
    public Integer getSpotCursorIndex(String spot) {
        return cursorIndex.get(spot);
    }

    public void preparePopup(String text) {
        popupText = text;
    }

    @Override
    public void setTenantId(Integer id) {
        SpotRestServiceBuilder.getSpotList(calendar.getTenantId(), new FailureShownRestCallback<List<Spot>>() {
            @Override
            public void onSuccess(List<Spot> spotList) {
                spots = spotList;
            }
        });
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (event.getAssociatedType().equals(RowCountChangeEvent.getType())) {
            rowCountHandlers.forEach((h) -> h.onRowCountChange((RowCountChangeEvent) event));
        }
        else if (event.getAssociatedType().equals(RangeChangeEvent.getType())) {
            rangeHandlers.forEach((h) -> h.onRangeChange((RangeChangeEvent) event));
        }
    }

    @Override
    public HandlerRegistration addRangeChangeHandler(Handler handler) {
        rangeHandlers.add(handler);
        return new Registration<>(handler, rangeHandlers);
    }

    @Override
    public HandlerRegistration addRowCountChangeHandler(com.google.gwt.view.client.RowCountChangeEvent.Handler handler) {
        rowCountHandlers.add(handler);
        return new Registration<>(handler, rowCountHandlers);
    }

    @Override
    public int getRowCount() {
        return totalSpotSlots;
    }

    @Override
    public Range getVisibleRange() {
        return new Range(rangeStart, rangeEnd - rangeStart);
    }

    @Override
    public boolean isRowCountExact() {
        return true;
    }

    @Override
    public void setRowCount(int count) {
        //Unimplemented; we control the rows
    }

    @Override
    public void setRowCount(int count, boolean isExact) {
        //Unimplemented; we control the rows
    }

    @Override
    public void setVisibleRange(int start, int length) {
        rangeStart = start;
        rangeEnd = start + length;
    }

    @Override
    public void setVisibleRange(Range range) {
        rangeStart = range.getStart();
        rangeEnd = range.getStart() + range.getLength();
    }

    @Override
    public HandlerRegistration addCellPreviewHandler(com.google.gwt.view.client.CellPreviewEvent.Handler<Collection<ShiftDrawable>> handler) {
        return new Registration<com.google.gwt.view.client.CellPreviewEvent.Handler<Collection<ShiftDrawable>>>();
    }

    @Override
    public SelectionModel<? super Collection<ShiftDrawable>> getSelectionModel() {
        return selectionModel;
    }

    @Override
    public Collection<ShiftDrawable> getVisibleItem(int indexOnPage) {
        return shiftDrawables.stream()
                .filter((d) -> d.getIndex() + spotPos.get(d.getGroupId()) - rangeStart == indexOnPage)
                .collect(Collectors.toSet());
    }

    @Override
    public int getVisibleItemCount() {
        return shiftDrawables.stream()
                .map((d) -> d.getIndex() + spotPos.get(d.getGroupId()))
                .filter((i) -> i >= rangeStart && i <= rangeEnd).collect(Collectors.toSet()).size();
    }

    @Override
    public Iterable<Collection<ShiftDrawable>> getVisibleItems() {
        HashMap<Integer,Set<ShiftDrawable>> out = new HashMap<>();
        shiftDrawables.stream()
                .forEach((d) -> {
                    int index = d.getIndex() + spotPos.get(d.getGroupId());
                    if (rangeStart <= index && index <= rangeEnd) {
                        Set<ShiftDrawable> group = out.getOrDefault(index, new HashSet<>());
                        group.add(d);
                        out.put(index, group);
                    }
                });
        
        return out.keySet().stream().sorted().map((k) -> out.get(k))
                .collect(Collectors.toList());
    }
    
    public List<Collection<ShiftDrawable>> getItems() {
        HashMap<Integer,Set<ShiftDrawable>> out = new HashMap<>();
        shiftDrawables.stream()
                .forEach((d) -> {
                    int index = d.getIndex() + spotPos.get(d.getGroupId());
                    Set<ShiftDrawable> group = out.getOrDefault(index, new HashSet<>());
                    group.add(d);
                    out.put(index, group);
                });
        return out.keySet().stream().sorted().map((k) -> out.get(k))
                .collect(Collectors.toList());
    }

    @Override
    public void setRowData(int start, List<? extends Collection<ShiftDrawable>> values) {
        
    }

    @Override
    public void setSelectionModel(SelectionModel<? super Collection<ShiftDrawable>> selectionModel) {
        this.selectionModel = selectionModel;
    }

    @Override
    public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
        setVisibleRange(range);
        calendar.draw();
    }
    
    private class Registration<T> implements HandlerRegistration {
        Collection<T> backingCollection;
        T handler;
        
        public Registration(T handler, Collection<T> backingCollection) {
            this.handler = handler;
            this.backingCollection = backingCollection;
        }
        
        public Registration() {
            backingCollection = new HashSet<>();
        }
        
        @Override
        public void removeHandler() {
            backingCollection.remove(handler);
        }
    }

}
