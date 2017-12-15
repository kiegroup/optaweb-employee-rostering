package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import elemental2.dom.MouseEvent;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Drawable.PostMouseDownEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.Value;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlotUtils;

public class TwoDayViewPresenter<G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G>> implements
        CalendarPresenter<G,
                I>, HasRows, HasData<
                        Collection<D>> {

    public static final int SECONDS_PER_MINUTE = 60;
    public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60;
    public static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;

    public static final String[] WEEKDAYS = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().weekdaysFull();
    public static final int WEEK_START = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().firstDayOfTheWeek();

    public static final int HEADER_HEIGHT = 64;
    public static final double SPOT_NAME_WIDTH = 200;

    private Calendar<G, I> calendar;
    private TwoDayView<G, I, D> view;

    private List<G> groups = new ArrayList<>();
    private Collection<Handler> rangeHandlers = new ArrayList<>();
    private Collection<com.google.gwt.view.client.RowCountChangeEvent.Handler> rowCountHandlers = new ArrayList<>();
    private HashMap<G, Integer> groupPos = new HashMap<>();
    private HashMap<G, Integer> groupIndexOf = new HashMap<>();
    private HashMap<G, Integer> groupEndPos = new HashMap<>();
    private HashMap<G, Integer> cursorIndex = new HashMap<>();
    private HashMap<G, DynamicContainer> groupContainer = new HashMap<>();
    private HashMap<G, DynamicContainer> groupAddPlane = new HashMap<>();
    private Collection<I> shifts;

    private TimeSlotTable<D, G> timeslotTable;

    private Collection<D> shiftDrawables;
    private List<Collection<D>> cachedVisibleItems;
    private List<Collection<D>> allItems;
    private int rangeStart, rangeEnd;
    private int totalDisplayedSpotSlots;

    private double mouseX, mouseY;
    private double localMouseX, localMouseY;
    private double dragStartX, dragStartY;

    private double screenWidth, screenHeight;
    private double widthPerMinute, spotHeight;
    private int totalSpotSlots;
    private int daysShown;
    private int editMinuteGradality;
    private int displayMinuteGradality;

    private boolean isDragging, creatingEvent, visibleDirty, allDirty, isCreating;

    private LocalDateTime baseDate;
    private LocalDateTime currDay;
    private LocalDateTime hardStartDateBound;
    private LocalDateTime hardEndDateBound;

    private G selectedSpot;
    private Long selectedIndex;
    private G overSpot;
    private String popupText;
    private Drawable toolBox;
    private D mouseOverDrawable;

    private int page;
    private ListDataProvider<Collection<D>> dataProvider = new ListDataProvider<>();
    private SelectionModel<? super Collection<D>> selectionModel;

    private TimeRowDrawableProvider<G, I, D> drawableProvider;

    private double scrollBarPos;
    private int scrollBarLength;
    private int scrollBarHandleLength;

    private DateDisplay dateFormat;
    private TranslationService translator;

    public TwoDayViewPresenter(Calendar<G, I> calendar, TimeRowDrawableProvider<G, I,
            D> drawableProvider, DateDisplay dateDisplay, TranslationService translator) {
        this.calendar = calendar;
        this.translator = translator;
        baseDate = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        currDay = baseDate;
        mouseX = 0;
        mouseY = 0;
        localMouseX = 0;
        localMouseY = 0;
        rangeStart = 0;
        rangeEnd = 10;
        totalDisplayedSpotSlots = 10;
        daysShown = 5;
        editMinuteGradality = 30;
        displayMinuteGradality = 60 * 3;
        dateFormat = dateDisplay;
        selectedSpot = null;
        isDragging = false;
        creatingEvent = false;
        popupText = null;
        toolBox = null;
        selectedIndex = null;
        shiftDrawables = new ArrayList<>();
        visibleDirty = true;
        allDirty = true;
        isCreating = false;
        screenWidth = 1;
        screenHeight = 1;
        selectionModel = new NoSelectionModel<Collection<? extends D>>((g) -> (g.isEmpty()) ? null : g.iterator().next()
                .getGroupId());
        this.drawableProvider = drawableProvider;
        mouseOverDrawable = null;
        timeslotTable = new TimeSlotTable<D, G>(shiftDrawables, groupPos, getViewStartDate(), getViewEndDate());
        dataProvider.addDataDisplay(this);
        scrollBarPos = 0;
        scrollBarLength = 1;
        scrollBarHandleLength = 1;
        view = TwoDayView.create(calendar.getBeanManager(), this);
    }

    // Getter/Setters
    public TranslationService getTranslator() {
        return translator;
    }

    public DateDisplay getDateFormat() {
        return dateFormat;
    }

    @Override
    public void setDate(LocalDateTime date) {
        visibleDirty = true;
        currDay = date;//LocalDateTime.of(date.toLocalDate(), LocalTime.MIDNIGHT);
        timeslotTable.setStartDate(getViewStartDate());
        timeslotTable.setEndDate(getViewEndDate());
        draw();
    }

    @Override
    public LocalDateTime getHardStartDateBound() {
        return hardStartDateBound;
    }

    @Override
    public void setHardStartDateBound(LocalDateTime hardStartDateBound) {
        this.hardStartDateBound = hardStartDateBound;
        if (getViewStartDate().isBefore(hardStartDateBound)) {
            setDate(hardStartDateBound);
        }
        draw();
    }

    @Override
    public LocalDateTime getHardEndDateBound() {
        return hardEndDateBound;
    }

    @Override
    public void setHardEndDateBound(LocalDateTime hardEndDateBound) {
        this.hardEndDateBound = hardEndDateBound;
        if (getViewEndDate().isAfter(hardEndDateBound)) {
            setDate(hardEndDateBound.minusDays(daysShown));
        }
        draw();
    }

    public int getDaysShown() {
        return daysShown;
    }

    public void setDaysShown(int daysShown) {
        if (this.daysShown == daysShown) {
            return;
        }
        this.daysShown = daysShown;
        setToolBox(null);
        calendar.setViewSize(screenWidth, screenHeight);
        draw();
    }

    public int getEditMinuteGradality() {
        return editMinuteGradality;
    }

    public void setEditMinuteGradality(int editMinuteGradality) {
        this.editMinuteGradality = editMinuteGradality;
    }

    public int getDisplayMinuteGradality() {
        return displayMinuteGradality;
    }

    public void setDisplayMinuteGradality(int displayMinuteGradality) {
        this.displayMinuteGradality = displayMinuteGradality;
    }

    @Override
    public List<G> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    @Override
    public void setGroups(List<G> groups) {
        this.groups = groups.stream().sorted((a, b) -> CommonUtils.stringWithIntCompareTo(a.getTitle(), b.getTitle()))
                .collect(Collectors
                        .toList());
    }

    public Calendar<G, I> getCalendar() {
        return calendar;
    }

    public double getScrollBarPos() {
        return scrollBarPos;
    }

    public int getScrollBarLength() {
        return scrollBarLength;
    }

    public int getScrollBarHandleLength() {
        return scrollBarHandleLength;
    }

    public LocalDateTime getBaseDate() {
        return baseDate;
    }

    public G getSelectedSpot() {
        return selectedSpot;
    }

    public void setSelectedSpot(G selectedSpot) {
        this.selectedSpot = selectedSpot;
    }

    public Long getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(Long selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public G getOverSpot() {
        return overSpot;
    }

    public void setOverSpot(G overSpot) {
        this.overSpot = overSpot;
    }

    public String getPopupText() {
        return popupText;
    }

    public void setPopupText(String popupText) {
        this.popupText = popupText;
    }

    public Drawable getToolBox() {
        return toolBox;
    }

    public void setToolBox(Drawable d) {
        toolBox = d;
    }

    public D getMouseOverDrawable() {
        return mouseOverDrawable;
    }

    public void setMouseOverDrawable(D mouseOverDrawable) {
        this.mouseOverDrawable = mouseOverDrawable;
    }

    public double getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(double screenWidth) {
        this.screenWidth = screenWidth;
    }

    public double getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(double screenHeight) {
        this.screenHeight = screenHeight;
    }

    // Calculated fields getters
    public double getLocationOfGroupSlot(G group, int slot) {
        return HEADER_HEIGHT + (getGroupPos().get(group) + slot) * getGroupHeight() - getOffsetY();
    }

    public LocalDateTime getViewStartDate() {
        return currDay;
    }

    public LocalDateTime getViewEndDate() {
        return currDay.plusDays(daysShown);
    }

    public boolean isDragging() {
        return isDragging;
    }

    public boolean isCreatingEvent() {
        return creatingEvent;
    }

    public boolean isCreating() {
        return isCreating;
    }

    public double getWidthPerMinute() {
        return widthPerMinute;
    }

    public double getGroupHeight() {
        return spotHeight;
    }

    public int getGroupIndex(G groupId) {
        return groupIndexOf.get(groupId);
    }

    public double getOffsetX() {
        return (view.getScreenWidth() - SPOT_NAME_WIDTH) * Math.round(getDifferenceFromBaseDate()) * (1.0
                / getDaysShown());
    }

    public double getOffsetY() {
        return (view.getScreenHeight() - HEADER_HEIGHT - spotHeight) * page;
    }

    public Integer getCursorIndex(G spot) {
        return cursorIndex.get(spot);
    }

    public HashMap<G, Integer> getGroupPos() {
        return groupPos;
    }

    public HashMap<G, Integer> getGroupEndPos() {
        return groupEndPos;
    }

    public HashMap<G, DynamicContainer> getGroupContainer() {
        return groupContainer;
    }

    public HashMap<G, DynamicContainer> getGroupAddPlane() {
        return groupAddPlane;
    }

    public void preparePopup(String text) {
        popupText = text;
    }

    public void setPage(int page) {
        if (this.page == page) {
            return;
        }
        this.page = page;
        setToolBox(null);
        draw();
    }

    @Override
    public Collection<G> getVisibleGroups() {
        int index = 0;
        Set<G> drawnSpots = new HashSet<>();
        int groupIndex = getGroupIndex(groupPos.keySet().stream().filter((group) -> groupEndPos.get(
                group) >= rangeStart).min((a, b) -> groupEndPos.get(a) - groupEndPos.get(b)).orElseGet(() -> groups.get(
                        0)));

        drawnSpots.add(groups.get(groupIndex));

        for (Collection<D> group : getVisibleItems()) {
            if (!group.isEmpty()) {
                index++;
            } else {
                index++;
                if (groupIndex < groups.size() && rangeStart + index > groupEndPos.getOrDefault(groups.get(groupIndex),
                        rangeStart + index)) {
                    groupIndex++;
                    if (groupIndex < groups.size()) {
                        drawnSpots.add(groups.get(groupIndex));
                    }
                }
            }
        }

        return drawnSpots;
    }

    public int getTotalDisplayedSpotSlots() {
        return totalDisplayedSpotSlots;
    }

    // Date Calculations
    public double getLocationOfDate(LocalDateTime date) {
        return ((date.toEpochSecond(ZoneOffset.UTC) - currDay.toEpochSecond(ZoneOffset.UTC)) / 60)
                * getWidthPerMinute() + SPOT_NAME_WIDTH;

    }

    public double getDaysBetweenEndpoints() {
        if (hardStartDateBound != null && hardEndDateBound != null) {
            return (hardEndDateBound.toEpochSecond(ZoneOffset.UTC) -
                    hardStartDateBound.toEpochSecond(ZoneOffset.UTC) + 0.0) / SECONDS_PER_DAY;
        }
        return 0;
    }

    public double getDifferenceFromBaseDate() {
        return (currDay.toEpochSecond(ZoneOffset.UTC) - baseDate.toEpochSecond(ZoneOffset.UTC)) / (SECONDS_PER_DAY
                + 0.0);
    }

    public LocalDateTime roundLocalDateTime(LocalDateTime toRound) {
        long fromMins = Math.round(toRound.toEpochSecond(ZoneOffset.UTC) / (60.0 * editMinuteGradality))
                * editMinuteGradality;
        return LocalDateTime.ofEpochSecond(60 * fromMins, 0, ZoneOffset.UTC).plusDays(Math.round(
                getDifferenceFromBaseDate()));
    }

    // Shift Calculations
    private List<HasTimeslot<G>> getShiftsDuring(I time, Collection<? extends HasTimeslot<G>> shifts) {
        return shifts.stream().filter((shift) -> TimeSlotUtils.doTimeslotsIntersect(time.getStartTime(), time
                .getEndTime(), shift
                        .getStartTime(), shift.getEndTime())).collect(Collectors.toList());
    }

    @Override
    public void addShift(I shift) {
        //TODO: Make this better
        setShifts(calendar.getShifts());
    }

    @Override
    public void removeShift(I shift) {
        //TODO: Make this better
        setShifts(calendar.getShifts());
    }

    @Override
    public void setShifts(Collection<I> shifts) {
        this.shifts = shifts;
        shiftDrawables = new ArrayList<>();
        totalSpotSlots = 0;
        groupPos.clear();
        groupEndPos.clear();
        groupContainer.clear();
        groupAddPlane.clear();
        groupIndexOf.clear();
        cursorIndex.clear();
        allDirty = true;
        visibleDirty = true;
        mouseOverDrawable = null;
        HashMap<G, HashMap<I, Set<Integer>>> placedSpots = new HashMap<>();
        HashMap<G, String> colorMap = new HashMap<>();
        int groupIndex = 0;
        for (G group : groups) {
            groupIndexOf.put(group, groupIndex);
            HashMap<I, Set<Integer>> placedShifts = new HashMap<>();
            int max = -1;
            groupPos.put(group, totalSpotSlots);
            final long spotStartPos = totalSpotSlots;
            groupContainer.put(group, new DynamicContainer(() -> new Position(SPOT_NAME_WIDTH, HEADER_HEIGHT
                    + spotStartPos * getGroupHeight())));
            colorMap.put(group, ColorUtils.getColor(colorMap.size()));

            for (I shift : shifts.stream().filter((s) -> s.getGroupId().equals(group)).collect(Collectors.toList())) {
                List<HasTimeslot<G>> concurrentShifts = getShiftsDuring(shift, placedShifts.keySet());
                HashMap<HasTimeslot<G>, Set<Integer>> concurrentPlacedShifts = new HashMap<>();
                placedShifts.forEach((k, v) -> {
                    if (concurrentShifts.contains(k)) {
                        concurrentPlacedShifts.put(k, v);
                    }
                });
                int index = 0;
                final Value<Integer> i = new Value<>(0);
                while (concurrentPlacedShifts.values().stream().anyMatch((s) -> s.contains(i.get()))) {
                    index++;
                    i.set(index);
                }
                Set<Integer> indicies = placedShifts.getOrDefault(shift, new HashSet<>());
                indicies.add(index);
                placedShifts.putIfAbsent(shift, indicies);
                max = Math.max(max, index);
            }

            totalSpotSlots += max + 2;
            final int spotEndPos = totalSpotSlots;
            groupEndPos.put(group, spotEndPos - 1);
            groupAddPlane.put(group, new DynamicContainer(() -> new Position(SPOT_NAME_WIDTH, HEADER_HEIGHT
                    + getGroupHeight() * (spotEndPos - 1))));
            placedSpots.put(group, placedShifts);
            groupIndex++;
        }

        for (I shift : shifts) {
            if (placedSpots.containsKey(shift.getGroupId()) && placedSpots.get(shift.getGroupId()).containsKey(shift)) {
                for (Integer index : placedSpots.get(shift.getGroupId()).get(shift)) {
                    D drawable = drawableProvider.createDrawable(this, shift, index);
                    drawable.setParent(groupContainer.get(shift.getGroupId()));
                    shiftDrawables.add(drawable);
                }
            }
        }

        timeslotTable = new TimeSlotTable<D, G>(shiftDrawables, groupPos, getViewStartDate(), getViewEndDate());

        for (G spot : groups) {
            cursorIndex.put(spot, groupEndPos.get(spot));
        }

        dataProvider.setList(getItems());
        dataProvider.flush();
        view.updatePager();
        draw();
    }

    // Mouse Handling
    public LocalDateTime getMouseLocalDateTime() {
        try {
            return currDay.plusMinutes(Math.round((localMouseX - SPOT_NAME_WIDTH) / getWidthPerMinute()));
        } catch (DateTimeException e) {
            return baseDate;
        }
    }

    private void handleMouseDown(double eventX, double eventY) {
        double offsetX = getOffsetX();
        for (G spot : groupAddPlane.keySet()) {
            if (groupContainer.get(spot).getGlobalX() < mouseX - offsetX && groupContainer.get(spot)
                    .getGlobalY() < mouseY && mouseY < groupAddPlane.get(spot).getGlobalY() + spotHeight) {
                int index = (int) Math.floor((mouseY - groupContainer.get(spot).getGlobalY()) / spotHeight);
                if (null != overSpot) {
                    cursorIndex.put(overSpot, groupEndPos.get(overSpot));
                }
                selectedSpot = spot;
                overSpot = spot;
                cursorIndex.put(overSpot, index);
                isCreating = true;
                selectedIndex = (long) Math.floor((mouseY - groupContainer.get(spot).getGlobalY()) / spotHeight);
                break;
            }
        }
    }

    private void handleMouseUp(double eventX, double eventY) {
        if (null != selectedSpot) {
            long fromMins = Math.round((dragStartX - SPOT_NAME_WIDTH - getOffsetX()) / (widthPerMinute
                    * editMinuteGradality)) * editMinuteGradality;
            LocalDateTime from = LocalDateTime.ofEpochSecond(60 * fromMins, 0, ZoneOffset.UTC).plusSeconds(
                    currDay.toEpochSecond(ZoneOffset.UTC) - baseDate.toEpochSecond(ZoneOffset.UTC));
            long toMins = Math.max(0, Math.round((mouseX - SPOT_NAME_WIDTH - getOffsetX()) / (widthPerMinute
                    * editMinuteGradality))) * editMinuteGradality;
            LocalDateTime to = LocalDateTime.ofEpochSecond(60 * toMins, 0, ZoneOffset.UTC).plusSeconds(
                    currDay.toEpochSecond(ZoneOffset.UTC) - baseDate.toEpochSecond(ZoneOffset.UTC));
            if (to.isBefore(from)) {
                LocalDateTime tmp = to;
                to = from;
                from = tmp;
            }
            calendar.addShift(selectedSpot, from, to);
        }
    }

    @Override
    public void onMouseDown(MouseEvent e) {
        localMouseX = CanvasUtils.getCanvasX(view.getCanvas(), e);
        localMouseY = CanvasUtils.getCanvasY(view.getCanvas(), e);
        mouseX = localMouseX + getOffsetX();
        mouseY = localMouseY + getOffsetY();
        dragStartX = mouseX;
        dragStartY = mouseY;
        isDragging = true;

        PostMouseDownEvent consumed = PostMouseDownEvent.IGNORE;
        if (null != toolBox) {
            consumed = toolBox.onMouseDown(e, localMouseX, localMouseY);
            if (PostMouseDownEvent.REMOVE_FOCUS == consumed) {
                draw();
                return;
            } else {
                toolBox = null;
            }
        }
        for (D drawable : CommonUtils.flatten(getVisibleItems())) {
            LocalDateTime mouseTime = getMouseLocalDateTime();
            double drawablePos = drawable.getGlobalY();

            if (mouseY >= drawablePos && mouseY <= drawablePos + getGroupHeight()) {
                if (mouseTime.isBefore(drawable.getEndTime()) && mouseTime.isAfter(drawable.getStartTime())) {
                    mouseOverDrawable = drawable;
                    consumed = drawable.onMouseDown(e, mouseX, mouseY);
                    break;
                }
            }
        }
        if (consumed == PostMouseDownEvent.IGNORE) {
            handleMouseDown(mouseX, mouseY);
        } else if (consumed == PostMouseDownEvent.REMOVE_FOCUS) {
            isDragging = false;
        } else {
            selectedSpot = mouseOverDrawable.getGroupId();
            selectedIndex = (long) mouseOverDrawable.getIndex();
            overSpot = selectedSpot;
            cursorIndex.put(overSpot, selectedIndex.intValue());
        }

        draw();
    }

    @Override
    public void onMouseUp(MouseEvent e) {
        localMouseX = CanvasUtils.getCanvasX(view.getCanvas(), e);
        localMouseY = CanvasUtils.getCanvasY(view.getCanvas(), e);
        mouseX = localMouseX + getOffsetX();
        mouseY = localMouseY + getOffsetY();
        isCreating = false;

        boolean consumed = false;
        if (mouseOverDrawable != null) {
            consumed = mouseOverDrawable.onMouseUp(e, mouseX, mouseY);
            //cursorIndex.put(mouseOverDrawable.getGroupId(), mouseOverDrawable.getIndex());
        }
        if (!consumed) {
            mouseOverDrawable = null;
            handleMouseUp(mouseX, mouseY);
        }

        isDragging = false;
        selectedSpot = null;
        selectedIndex = 0L;

        draw();
    }

    @Override
    public void onMouseMove(MouseEvent e) {
        localMouseX = CanvasUtils.getCanvasX(view.getCanvas(), e);
        localMouseY = CanvasUtils.getCanvasY(view.getCanvas(), e);
        mouseX = localMouseX + getOffsetX();
        mouseY = localMouseY + getOffsetY();
        boolean consumed = false;
        boolean foundDrawable = false;

        if (isDragging) {
            if (mouseOverDrawable != null) {
                consumed = mouseOverDrawable.onMouseDrag(e, mouseX, mouseY);
            }
            if (!consumed) {
                mouseOverDrawable = null;
                onMouseDrag(mouseX, mouseY);
            }

        } else {
            if (null != toolBox) {
                if (toolBox.onMouseMove(e, localMouseX, localMouseY)) {
                    draw();
                    return;
                }
            }
            for (D drawable : CommonUtils.flatten(getVisibleItems())) {
                LocalDateTime mouseTime = getMouseLocalDateTime();
                double drawablePos = drawable.getGlobalY();

                if (mouseY >= drawablePos && mouseY <= drawablePos + getGroupHeight()) {
                    if (mouseTime.isBefore(drawable.getEndTime()) && mouseTime.isAfter(drawable.getStartTime())) {
                        if (drawable != mouseOverDrawable) {
                            if (null != mouseOverDrawable) {
                                mouseOverDrawable.onMouseExit(e, mouseX, mouseY);
                            }
                            mouseOverDrawable = drawable;
                            drawable.onMouseEnter(e, mouseX, mouseY);
                        }
                        foundDrawable = true;
                        consumed = drawable.onMouseMove(e, mouseX, mouseY);
                        break;
                    }
                }
            }
            if (!foundDrawable && null != mouseOverDrawable) {
                mouseOverDrawable.onMouseExit(e, mouseX, mouseY);
                mouseOverDrawable = null;
            }
        }

        draw();
    }

    private void onMouseDrag(double x, double y) {
    }

    public double getGlobalMouseX() {
        return mouseX;
    }

    public double getGlobalMouseY() {
        return mouseY;
    }

    public double getLocalMouseX() {
        return localMouseX;
    }

    public double getLocalMouseY() {
        return localMouseY;
    }

    public double getDragStartX() {
        return dragStartX;
    }

    public double getDragStartY() {
        return dragStartY;
    }

    // HasRows/HasData methods
    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (event.getAssociatedType().equals(RowCountChangeEvent.getType())) {
            rowCountHandlers.forEach((h) -> h.onRowCountChange((RowCountChangeEvent) event));
        } else if (event.getAssociatedType().equals(RangeChangeEvent.getType())) {
            rangeHandlers.forEach((h) -> h.onRangeChange((RangeChangeEvent) event));
        }
    }

    @Override
    public HandlerRegistration addRangeChangeHandler(Handler handler) {
        rangeHandlers.add(handler);
        return new Registration<>(handler, rangeHandlers);
    }

    @Override
    public HandlerRegistration addRowCountChangeHandler(
            com.google.gwt.view.client.RowCountChangeEvent.Handler handler) {
        rowCountHandlers.add(handler);
        return new Registration<>(handler, rowCountHandlers);
    }

    @Override
    public int getRowCount() {
        if (allDirty) {
            getItems();
        }
        return allItems.size();
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
        if (start == rangeStart && rangeEnd - rangeStart == length) {
            return;
        }
        visibleDirty = true;
        rangeStart = start;
        rangeEnd = start + length;
        draw();
    }

    @Override
    public void setVisibleRange(Range range) {
        if (range.getStart() == rangeStart && rangeEnd - rangeStart == range.getLength()) {
            return;
        }
        visibleDirty = true;
        rangeStart = range.getStart();
        rangeEnd = range.getStart() + range.getLength();
        draw();
    }

    @Override
    public HandlerRegistration addCellPreviewHandler(com.google.gwt.view.client.CellPreviewEvent.Handler<Collection<
            D>> handler) {
        return new Registration<com.google.gwt.view.client.CellPreviewEvent.Handler<Collection<ShiftDrawable>>>();
    }

    @Override
    public SelectionModel<? super Collection<D>> getSelectionModel() {
        return selectionModel;
    }

    @Override
    public Collection<D> getVisibleItem(int indexOnPage) {
        if (visibleDirty) {
            getVisibleItems();
        }
        return cachedVisibleItems.get(indexOnPage);
    }

    @Override
    public int getVisibleItemCount() {
        if (visibleDirty) {
            getVisibleItems();
        }
        return cachedVisibleItems.size();
    }

    @Override
    public Iterable<Collection<D>> getVisibleItems() {
        if (visibleDirty) {
            cachedVisibleItems = IntStream.range(rangeStart, rangeEnd).mapToObj((k) -> timeslotTable.getVisableRow(k))
                    .collect(
                            Collectors.toList());
            visibleDirty = false;
        }
        return cachedVisibleItems;
    }

    public List<Collection<D>> getItems() {
        if (allDirty) {
            allItems = IntStream.range(0, totalSpotSlots).mapToObj((k) -> timeslotTable.getRow(k)).collect(Collectors
                    .toList());
            allDirty = false;
        }
        return allItems;
    }

    @Override
    public void setRowData(int start, List<? extends Collection<D>> values) {

    }

    @Override
    public void setSelectionModel(SelectionModel<? super Collection<D>> selectionModel) {
        this.selectionModel = selectionModel;
    }

    @Override
    public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
        setVisibleRange(range);
        draw();
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

    // View defer
    public void draw() {
        if (null != hardStartDateBound && null != hardEndDateBound) {
            int daysBetween = (int) ((hardEndDateBound.toEpochSecond(ZoneOffset.UTC) - hardStartDateBound.toEpochSecond(
                    ZoneOffset.UTC)) / (60 * 60 * 24));
            scrollBarLength = (int) Math.round((daysBetween + 0.0) / getDaysShown());
            scrollBarHandleLength = daysShown;
            scrollBarPos = (currDay.toEpochSecond(ZoneOffset.UTC) -
                    getViewStartDate().toEpochSecond(ZoneOffset.UTC) + 0.0) / (SECONDS_PER_DAY * daysBetween);
        } else {
            scrollBarPos = 0;
            scrollBarLength = 0;
            scrollBarHandleLength = 0;
        }
        view.updateScrollBars();
        view.setViewSize(screenWidth, screenHeight);
        view.draw();
    }

    public void setViewSize(double screenWidth, double screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        view.setViewSize(screenWidth, screenHeight);
        widthPerMinute = (view.getScreenWidth() - SPOT_NAME_WIDTH) / (daysShown * (SECONDS_PER_DAY
                / SECONDS_PER_MINUTE));
        spotHeight = (view.getScreenHeight() - HEADER_HEIGHT) / (totalDisplayedSpotSlots + 1);
    }

    @Override
    public HTMLElement getElement() {
        return view.getElement();
    }

}