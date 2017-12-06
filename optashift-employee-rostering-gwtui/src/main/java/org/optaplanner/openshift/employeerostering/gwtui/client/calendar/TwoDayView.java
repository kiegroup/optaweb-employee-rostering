package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.google.gwt.user.cellview.client.SimplePager;
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
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Drawable.PostMouseDownEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.Value;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlotUtils;

public class TwoDayView<G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G>> implements
        CalendarView<G,
                I>, HasRows, HasData<
                        Collection<D>> {

    Calendar<G, I> calendar;

    private static final String BACKGROUND_1 = "#efefef";
    private static final String BACKGROUND_2 = "#e0e0e0";
    private static final String LINE_COLOR = "#000000";
    private static final String[] WEEKDAYS = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().weekdaysFull();
    private static final int WEEK_START = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().firstDayOfTheWeek();

    private static final int HEADER_HEIGHT = 64;
    private static final double SPOT_NAME_WIDTH = 200;

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
    private MockScrollBar scrollBar;

    TimeSlotTable<D, G> timeslotTable;

    private Collection<D> shiftDrawables;
    private List<Collection<D>> cachedVisibleItems;
    private List<Collection<D>> allItems;
    private Pagination pagination;
    private SimplePager pager;
    private ListDataProvider<Collection<D>> dataProvider = new ListDataProvider<>();
    private SelectionModel<? super Collection<D>> selectionModel;
    private int rangeStart, rangeEnd;
    private int totalDisplayedSpotSlots;
    LocalDateTime baseDate;
    LocalDateTime currDay;
    LocalDateTime hardStartDateBound;

    @Override
    public LocalDateTime getHardStartDateBound() {
        return hardStartDateBound;
    }

    @Override
    public void setHardStartDateBound(LocalDateTime hardStartDateBound) {
        this.hardStartDateBound = hardStartDateBound;
        if (null != hardStartDateBound && null != hardEndDateBound) {
            int daysBetween = (int) ((hardEndDateBound.toEpochSecond(ZoneOffset.UTC) - hardStartDateBound.toEpochSecond(
                    ZoneOffset.UTC)) / (60 * 60 * 24));
            scrollBar.setContentSize(daysBetween);
            scrollBar.setContentShown(daysShown);
        } else {
            scrollBar.setContentShown(0);
        }
    }

    @Override
    public LocalDateTime getHardEndDateBound() {
        return hardEndDateBound;
    }

    @Override
    public void setHardEndDateBound(LocalDateTime hardEndDateBound) {
        this.hardEndDateBound = hardEndDateBound;
        if (null != hardStartDateBound && null != hardEndDateBound) {
            int daysBetween = (int) ((hardEndDateBound.toEpochSecond(ZoneOffset.UTC) - hardStartDateBound.toEpochSecond(
                    ZoneOffset.UTC)) / (60 * 60 * 24));
            scrollBar.setContentSize(daysBetween);
            scrollBar.setContentShown(daysShown);
        } else {
            scrollBar.setContentShown(0);
        }
    }

    LocalDateTime hardEndDateBound;

    double mouseX, mouseY;
    double localMouseX, localMouseY;
    double screenWidth, screenHeight;
    double dragStartX, dragStartY;
    double widthPerMinute, spotHeight;
    int totalSpotSlots;
    int daysShown;
    int editMinuteGradality;
    int displayMinuteGradality;

    public int getDaysShown() {
        return daysShown;
    }

    public void setDaysShown(int daysShown) {
        scrollBar.setContentShown(daysShown);
        this.daysShown = daysShown;
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

    boolean isDragging, creatingEvent, visibleDirty, allDirty, isCreating;
    G selectedSpot;
    Long selectedIndex;
    G overSpot;
    String popupText;
    D mouseOverDrawable;

    Panel topPanel, bottomPanel, sidePanel;
    TimeRowDrawableProvider<G, I, D> drawableProvider;

    DateDisplay dateFormat;
    TranslationService translator;

    public TwoDayView(Calendar<G, I> calendar, Panel top, Panel bottom, Panel side, TimeRowDrawableProvider<G, I,
            D> drawableProvider, DateDisplay dateDisplay, TranslationService translator, MockScrollBar scrollBar) {
        this.calendar = calendar;
        this.translator = translator;
        this.scrollBar = scrollBar;
        baseDate = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        currDay = baseDate;
        mouseX = 0;
        mouseY = 0;
        localMouseX = 0;
        localMouseY = 0;
        rangeStart = 0;
        rangeEnd = 10;
        totalDisplayedSpotSlots = 10;
        daysShown = 7;
        editMinuteGradality = 30;
        displayMinuteGradality = 60 * 3;
        dateFormat = dateDisplay;
        selectedSpot = null;
        isDragging = false;
        creatingEvent = false;
        popupText = null;
        selectedIndex = null;
        topPanel = top;
        bottomPanel = bottom;
        sidePanel = side;
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
        initPanels();
    }

    private double getDifferenceFromBaseDate() {
        return (currDay.toEpochSecond(ZoneOffset.UTC) - baseDate.toEpochSecond(ZoneOffset.UTC)) / (60 * 60 * 24.0);
    }

    private void initPanels() {
        Label title = new Label();
        title.setText("Configuration Editor");
        topPanel.add(title);

        pagination = new Pagination();
        pager = new SimplePager();

        bottomPanel.add(pagination);

        scrollBar.setContentSize(1);
        scrollBar.setContentIndex(0);
        scrollBar.setContentShown(0);

        scrollBar.setNextButtonAction(() -> {
            if (null != getHardEndDateBound()) {
                if (getViewEndDate().isBefore(getHardEndDateBound())) {
                    setDate(getViewStartDate().plusDays(1));
                    if (null != getHardStartDateBound()) {
                        int dayDiff = (int) ((getViewStartDate().toEpochSecond(ZoneOffset.UTC) - getHardStartDateBound()
                                .toEpochSecond(ZoneOffset.UTC)) / (60 * 60 * 24));
                        scrollBar.setContentIndex(dayDiff);
                    }
                    calendar.draw();
                }
            } else {
                setDate(getViewStartDate().plusDays(1));
                calendar.draw();
            }
        });

        scrollBar.setPreviousButtonAction(() -> {
            if (null != getHardStartDateBound()) {
                if (getViewStartDate().isAfter(getHardStartDateBound())) {
                    setDate(getViewStartDate().minusDays(1));
                    if (null != getHardEndDateBound()) {
                        int dayDiff = (int) ((getViewStartDate().toEpochSecond(ZoneOffset.UTC) - getHardStartDateBound()
                                .toEpochSecond(ZoneOffset.UTC)) / (60 * 60 * 24));
                        scrollBar.setContentIndex(dayDiff);
                    }
                    calendar.draw();
                }
            } else {
                setDate(getViewStartDate().minusDays(1));
                calendar.draw();
            }
        });

        pager.setDisplay(this);
        pager.setPageSize(totalDisplayedSpotSlots);
        pagination.clear();
        dataProvider.addDataDisplay(this);
    }

    @Override
    public void draw(CanvasRenderingContext2D g, double screenWidth, double screenHeight) {
        g.clearRect(0, 0, screenWidth, screenHeight);
        long secondsPerDay = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC).plusDays(1).toEpochSecond(
                ZoneOffset.UTC);
        long minutesPerDay = secondsPerDay / 60;
        widthPerMinute = (screenWidth - SPOT_NAME_WIDTH) / (daysShown * minutesPerDay);
        spotHeight = (screenHeight - HEADER_HEIGHT) / (totalDisplayedSpotSlots + 1);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        drawShiftsBackground(g);
        drawSpots(g);
        drawTimes(g);
        drawSpotToCreate(g);
        drawPopup(g);
    }

    private void drawSpots(CanvasRenderingContext2D g) {
        if (groups.isEmpty()) {
            return;
        }

        int minSize = 12;//Integer.MAX_VALUE;
        //for (G spot : groups) {
        //    minSize = Math.min(minSize, CanvasUtils.fitTextToBox(g, spot.getTitle(), SPOT_NAME_WIDTH, spotHeight));
        //}

        g.save();
        g.translate(-getDifferenceFromBaseDate() * (60 * 24) * widthPerMinute, 0);
        int index = 0;
        Iterable<Collection<D>> toDraw = getVisibleItems();
        Set<G> drawnSpots = new HashSet<>();
        HashMap<G, Integer> spotIndex = new HashMap<>();
        int groupIndex = getGroupIndex(groupPos.keySet().stream().filter((group) -> groupEndPos.get(
                group) >= rangeStart).min((a, b) -> groupEndPos.get(a) - groupEndPos.get(b)).orElseGet(() -> groups.get(
                        0)));

        spotIndex.put(groups.get(groupIndex), index);
        drawnSpots.add(groups.get(groupIndex));

        for (Collection<D> group : toDraw) {
            if (!group.isEmpty()) {
                G groupId = group.iterator().next().getGroupId();

                for (D drawable : group) {
                    if (groupId.equals(selectedSpot) && drawable.getIndex() >= selectedIndex
                            && drawable != mouseOverDrawable) {
                        drawable.doDrawAt(g, drawable.getGlobalX(), HEADER_HEIGHT + (index + 1) * spotHeight);
                    } else {
                        drawable.doDrawAt(g, drawable.getGlobalX(), HEADER_HEIGHT + index * spotHeight);
                    }
                }
                index++;
            } else {
                index++;
                if (groupIndex < groups.size() && rangeStart + index > groupEndPos.getOrDefault(groups.get(groupIndex),
                        rangeStart + index)) {
                    groupIndex++;
                    if (groupIndex < groups.size()) {
                        spotIndex.put(groups.get(groupIndex), index);
                        drawnSpots.add(groups.get(groupIndex));
                    }
                }
            }
        }
        g.restore();

        CanvasUtils.setFillColor(g, "#FFFFFF");
        g.fillRect(0, HEADER_HEIGHT, SPOT_NAME_WIDTH, screenHeight - HEADER_HEIGHT);
        CanvasUtils.setFillColor(g, "#000000");
        double textHeight = CanvasUtils.getTextHeight(g, minSize);
        g.font = CanvasUtils.getFont(minSize);

        for (G spot : groups.stream().filter((s) -> drawnSpots.contains(s)).collect(Collectors.toList())) {
            int pos = spotIndex.get(spot);
            g.fillText(spot.getTitle(), 0, HEADER_HEIGHT + spotHeight * pos + textHeight + (spotHeight - textHeight)
                    / 2);
            CanvasUtils.drawLine(g, 0, HEADER_HEIGHT + spotHeight * pos, screenWidth, HEADER_HEIGHT + spotHeight * pos);
        }
    }

    private void drawPopup(CanvasRenderingContext2D g) {
        if (null != popupText) {
            g.font = CanvasUtils.getFont(12);
            double[] preferredSize = CanvasUtils.getPreferredBoxSizeForText(g, popupText, 12);
            g.strokeRect(getLocalMouseX() - preferredSize[0], getLocalMouseY() - preferredSize[1], preferredSize[0],
                    preferredSize[1]);
            CanvasUtils.setFillColor(g, "#B18800");
            g.fillRect(getLocalMouseX() - preferredSize[0], getLocalMouseY() - preferredSize[1], preferredSize[0],
                    preferredSize[1]);
            CanvasUtils.setFillColor(g, "#000000");
            CanvasUtils.drawTextInBox(g, popupText, getLocalMouseX() - preferredSize[0], getLocalMouseY()
                    - preferredSize[1], preferredSize[0], preferredSize[1]);
            popupText = null;
        }
    }

    private void drawShiftsBackground(CanvasRenderingContext2D g) {
        for (int x = 0; x < (screenWidth / (getWidthPerMinute() * displayMinuteGradality)); x++) {
            if (x % 2 == 0) {
                CanvasUtils.setFillColor(g, BACKGROUND_1);
            } else {
                CanvasUtils.setFillColor(g, BACKGROUND_2);
            }
            g.fillRect(SPOT_NAME_WIDTH + x * widthPerMinute * displayMinuteGradality, HEADER_HEIGHT, SPOT_NAME_WIDTH
                    + (x + 1)
                            * widthPerMinute * displayMinuteGradality, screenHeight - HEADER_HEIGHT);
        }
    }

    private void drawSpotToCreate(CanvasRenderingContext2D g) {
        if (isCreating) {
            CanvasUtils.setFillColor(g, "#00FF00");
            long fromMins = Math.round((dragStartX - SPOT_NAME_WIDTH - getOffsetX()) / (widthPerMinute
                    * editMinuteGradality)) * editMinuteGradality;
            LocalDateTime from = LocalDateTime.ofEpochSecond(60 * fromMins, 0, ZoneOffset.UTC).plusDays(Math.round(
                    getDifferenceFromBaseDate()));
            long toMins = Math.max(0, Math.round((mouseX - SPOT_NAME_WIDTH - getOffsetX()) / (widthPerMinute
                    * editMinuteGradality))) * editMinuteGradality;
            LocalDateTime to = LocalDateTime.ofEpochSecond(60 * toMins, 0, ZoneOffset.UTC).plusDays(Math.round(
                    getDifferenceFromBaseDate()));
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
            g.fillRect(dragStartX - getOffsetX(), groupContainer.get(selectedSpot).getGlobalY() + spotHeight
                    * selectedIndex - getOffsetY(), (toMins - fromMins) * widthPerMinute, spotHeight);
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

    public LocalDateTime roundLocalDateTime(LocalDateTime toRound) {
        long fromMins = Math.round(toRound.toEpochSecond(ZoneOffset.UTC) / (60.0 * editMinuteGradality))
                * editMinuteGradality;
        return LocalDateTime.ofEpochSecond(60 * fromMins, 0, ZoneOffset.UTC).plusDays(Math.round(
                getDifferenceFromBaseDate()));
    }

    private void handleMouseUp(double eventX, double eventY) {
        if (null != selectedSpot) {
            long fromMins = Math.round((dragStartX - SPOT_NAME_WIDTH - getOffsetX()) / (widthPerMinute
                    * editMinuteGradality)) * editMinuteGradality;
            LocalDateTime from = LocalDateTime.ofEpochSecond(60 * fromMins, 0, ZoneOffset.UTC).plusDays(Math.round(
                    getDifferenceFromBaseDate()));
            long toMins = Math.max(0, Math.round((mouseX - SPOT_NAME_WIDTH - getOffsetX()) / (widthPerMinute
                    * editMinuteGradality))) * editMinuteGradality;
            LocalDateTime to = LocalDateTime.ofEpochSecond(60 * toMins, 0, ZoneOffset.UTC).plusDays(Math.round(
                    getDifferenceFromBaseDate()));
            if (to.isBefore(from)) {
                LocalDateTime tmp = to;
                to = from;
                from = tmp;
            }
            calendar.addShift(selectedSpot, from, to);
        }
    }

    private void drawTimes(CanvasRenderingContext2D g) {
        CanvasUtils.setFillColor(g, "#000000");
        String week = dateFormat.format(currDay, WEEK_START, translator);
        int textSize = CanvasUtils.fitTextToBox(g, week, SPOT_NAME_WIDTH, HEADER_HEIGHT / 2);
        for (String day : WEEKDAYS) {
            textSize = Math.min(textSize, CanvasUtils.fitTextToBox(g, day, 24 * 60 * widthPerMinute,
                    HEADER_HEIGHT / 2));
        }
        g.font = CanvasUtils.getFont(textSize);
        g.fillText(week, 0, HEADER_HEIGHT / 2);
        int offset = (dateFormat != DateDisplay.WEEKS_FROM_EPOCH) ? 0 : LocalDateTime.ofEpochSecond(0, 0,
                ZoneOffset.UTC).getDayOfWeek().getValue();
        for (int x = 0; x < daysShown; x++) {
            g.fillText(WEEKDAYS[(int) (Math.abs((WEEK_START + x + currDay.getDayOfWeek().getValue() - 1 + offset))
                    % 7)],
                    SPOT_NAME_WIDTH + (24 * x) * 60 * widthPerMinute, HEADER_HEIGHT / 2);
            CanvasUtils.drawLine(g, SPOT_NAME_WIDTH + (24 * x) * 60 * widthPerMinute, 0, SPOT_NAME_WIDTH + (24 * x) * 60
                    * widthPerMinute, HEADER_HEIGHT);
        }
        //for (int x = 0; x < 8; x++) {
        //    g.fillText(((6 * x) % 24) + ":00", SPOT_NAME_WIDTH + x * 6 * 60 * widthPerMinute, HEADER_HEIGHT);
        //    CanvasUtils.drawLine(g, SPOT_NAME_WIDTH + x * 6 * widthPerMinute * 60, HEADER_HEIGHT, SPOT_NAME_WIDTH + x
        //            * 6 * widthPerMinute * 60, screenHeight);
        //}

        for (int x = 0; x < (screenWidth / (getWidthPerMinute() * displayMinuteGradality)); x++) {
            CanvasUtils.drawLine(g, SPOT_NAME_WIDTH + x * widthPerMinute * displayMinuteGradality, HEADER_HEIGHT,
                    SPOT_NAME_WIDTH + x * widthPerMinute * displayMinuteGradality, screenHeight);
        }
    }

    @Override
    public void onMouseDown(MouseEvent e) {
        localMouseX = CanvasUtils.getCanvasX(calendar.canvas, e);
        localMouseY = CanvasUtils.getCanvasY(calendar.canvas, e);
        mouseX = localMouseX + getOffsetX();
        mouseY = localMouseY + getOffsetY();
        dragStartX = mouseX;
        dragStartY = mouseY;
        isDragging = true;

        PostMouseDownEvent consumed = PostMouseDownEvent.IGNORE;
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
        }

        calendar.draw();
    }

    @Override
    public void onMouseUp(MouseEvent e) {
        localMouseX = CanvasUtils.getCanvasX(calendar.canvas, e);
        localMouseY = CanvasUtils.getCanvasY(calendar.canvas, e);
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

        calendar.draw();
    }

    public Calendar<G, I> getCalendar() {
        return calendar;
    }

    @Override
    public void onMouseMove(MouseEvent e) {
        localMouseX = CanvasUtils.getCanvasX(calendar.canvas, e);
        localMouseY = CanvasUtils.getCanvasY(calendar.canvas, e);
        mouseX = localMouseX + getOffsetX();
        mouseY = localMouseY + getOffsetY();
        boolean consumed = false;
        boolean foundDrawable = false;

        if (isDragging) {
            LocalDateTime mouseTime = getMouseLocalDateTime();
            if (mouseOverDrawable != null) {
                consumed = mouseOverDrawable.onMouseDrag(e, mouseX, mouseY);
            }
            if (!consumed) {
                mouseOverDrawable = null;
                onMouseDrag(mouseX, mouseY);
            }

        } else {
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

        calendar.draw();
    }

    private void onMouseDrag(double x, double y) {
    }

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
        pagination.rebuild(pager);
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

    public double getGlobalMouseX() {
        return mouseX;
    }

    public LocalDateTime getMouseLocalDateTime() {
        try {
            return currDay.plusMinutes(Math.round((localMouseX - SPOT_NAME_WIDTH) / getWidthPerMinute()));
        } catch (DateTimeException e) {
            return baseDate;
        }
    }

    public LocalDateTime getViewStartDate() {
        return currDay;
    }

    public LocalDateTime getViewEndDate() {
        return currDay.plusDays(daysShown);
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

    private double getOffsetX() {
        return (screenWidth - SPOT_NAME_WIDTH) * Math.round(getDifferenceFromBaseDate()) * 0.5;
    }

    private double getOffsetY() {
        return (screenHeight - HEADER_HEIGHT - spotHeight) * pager.getPage();
    }

    public double getDragStartX() {
        return dragStartX;
    }

    public double getDragStartY() {
        return dragStartY;
    }

    public Integer getCursorIndex(G spot) {
        return cursorIndex.get(spot);
    }

    public void preparePopup(String text) {
        popupText = text;
    }

    @Override
    public void setGroups(List<G> groups) {
        this.groups = groups.stream().sorted((a, b) -> CommonUtils.stringWithIntCompareTo(a.getTitle(), b.getTitle()))
                .collect(Collectors
                        .toList());
    }

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
        calendar.forceUpdate();
    }

    @Override
    public void setVisibleRange(Range range) {
        if (range.getStart() == rangeStart && rangeEnd - rangeStart == range.getLength()) {
            return;
        }
        visibleDirty = true;
        rangeStart = range.getStart();
        rangeEnd = range.getStart() + range.getLength();
        calendar.forceUpdate();
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

    @Override
    public void setDate(LocalDateTime date) {
        visibleDirty = true;
        currDay = LocalDateTime.of(date.toLocalDate(), LocalTime.MIDNIGHT);
        timeslotTable.setStartDate(getViewStartDate());
        timeslotTable.setEndDate(getViewEndDate());
        calendar.forceUpdate();
    }

    @Override
    public Collection<G> getGroups() {
        return Collections.unmodifiableList(groups);
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

}
