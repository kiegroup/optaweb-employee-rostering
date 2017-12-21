package org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.DynamicContainer;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Position;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeSlotTableView;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.Value;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlotTable;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlotUtils;

public class TwoDayViewState<G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G>> {

    private TwoDayViewPresenter<G, I, D> presenter;

    private List<G> groups = new ArrayList<>();
    private Collection<I> shifts;
    private Collection<D> shiftDrawables;

    private boolean visibleDirty, allDirty;

    private Map<G, TimeSlotTable<I>> timeSlotTables;
    private Map<G, List<List<TimeSlotTable.TimeSlot<I>>>> groupShifts;
    private TimeSlotTableView<D, G> timeSlotTableView;

    private double screenWidth, screenHeight;
    private double widthPerMinute, spotHeight;
    private int totalSpotSlots;

    private LocalDateTime baseDate;
    private LocalDateTime currDay;

    private double scrollBarPos;
    private int scrollBarLength;
    private int scrollBarHandleLength;

    private HashMap<G, Integer> groupPos = new HashMap<>();
    private HashMap<G, Integer> groupIndexOf = new HashMap<>();
    private HashMap<G, Integer> groupEndPos = new HashMap<>();
    private HashMap<G, DynamicContainer> groupContainer = new HashMap<>();
    private HashMap<G, DynamicContainer> groupAddPlane = new HashMap<>();

    public TwoDayViewState(TwoDayViewPresenter<G, I, D> presenter) {
        this.presenter = presenter;
        baseDate = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        currDay = baseDate;
        shiftDrawables = new ArrayList<>();
        visibleDirty = true;
        allDirty = true;
        screenWidth = 1;
        screenHeight = 1;
        timeSlotTables = new HashMap<>();
        groupShifts = new HashMap<>();
        timeSlotTableView = new TimeSlotTableView<D, G>(shiftDrawables, groupPos, getViewStartDate(), getViewEndDate());
        scrollBarPos = 0;
        scrollBarLength = 1;
        scrollBarHandleLength = 1;
    }

    public void setDate(LocalDateTime date) {
        visibleDirty = true;
        currDay = date;//LocalDateTime.of(date.toLocalDate(), LocalTime.MIDNIGHT);
        timeSlotTableView.setStartDate(getViewStartDate());
        timeSlotTableView.setEndDate(getViewEndDate());
        presenter.draw();
    }

    public List<G> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public void setGroups(List<G> groups) {
        this.groups = groups.stream().sorted((a, b) -> CommonUtils.stringWithIntCompareTo(a.getTitle(), b.getTitle()))
                .collect(Collectors
                        .toList());
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

    public void setScrollBarPos(double pos) {
        scrollBarPos = pos;
    }

    public void setScrollBarLength(int length) {
        scrollBarLength = length;
    }

    public void setScrollBarHandleLength(int length) {
        scrollBarHandleLength = length;
    }

    public LocalDateTime getBaseDate() {
        return baseDate;
    }

    public TimeSlotTableView<D, G> getTimeSlotTable() {
        return timeSlotTableView;
    }

    public LocalDateTime getViewStartDate() {
        return currDay;
    }

    public LocalDateTime getViewEndDate() {
        return currDay.plusDays(presenter.getConfig().getDaysShown());
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

    public double getLocationOfGroupSlot(G group, int slot) {
        return TwoDayViewPresenter.HEADER_HEIGHT + (groupPos.get(group) + slot) * getGroupHeight() - getOffsetY();
    }

    public boolean isDirty() {
        return visibleDirty;
    }

    public void setVisibleDirty(boolean isDirty) {
        visibleDirty = isDirty;
    }

    public boolean isAllDirty() {
        return allDirty;
    }

    public void setAllDirty(boolean isDirty) {
        allDirty = isDirty;
    }

    public double getWidthPerMinute() {
        return widthPerMinute;
    }

    public double getGroupHeight() {
        return spotHeight;
    }

    public void setWidthPerMinute(double width) {
        widthPerMinute = width;
    }

    public void setGroupHeight(double height) {
        spotHeight = height;
    }

    public int getGroupIndex(G groupId) {
        return groupIndexOf.get(groupId);
    }

    public double getOffsetX() {
        return getLocationOfDate(baseDate.plusSeconds(currDay.toEpochSecond(ZoneOffset.UTC) - baseDate.toEpochSecond(
                ZoneOffset.UTC)));
    }

    public double getOffsetY() {
        return (presenter.getView().getScreenHeight() - TwoDayViewPresenter.HEADER_HEIGHT - spotHeight) * presenter
                .getPage();
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

    public double getLocationOfDate(LocalDateTime date) {
        return ((date.toEpochSecond(ZoneOffset.UTC) - currDay.toEpochSecond(ZoneOffset.UTC)) / 60)
                * getWidthPerMinute() + TwoDayViewPresenter.SPOT_NAME_WIDTH;

    }

    public double getDaysBetweenEndpoints() {
        if (presenter.getConfig().getHardStartDateBound() != null && presenter.getConfig()
                .getHardEndDateBound() != null) {
            return (presenter.getConfig().getHardEndDateBound().toEpochSecond(ZoneOffset.UTC) -
                    presenter.getConfig().getHardStartDateBound().toEpochSecond(ZoneOffset.UTC) + 0.0)
                    / TwoDayViewPresenter.SECONDS_PER_DAY;
        }
        return 0;
    }

    public double getDifferenceFromBaseDate() {
        return (currDay.toEpochSecond(ZoneOffset.UTC) - baseDate.toEpochSecond(ZoneOffset.UTC))
                / (TwoDayViewPresenter.SECONDS_PER_DAY
                        + 0.0);
    }

    public LocalDateTime roundLocalDateTime(LocalDateTime toRound) {
        long fromMins = Math.round(toRound.toEpochSecond(ZoneOffset.UTC) / (60.0 * presenter.getConfig()
                .getEditMinuteGradality()))
                * presenter.getConfig().getEditMinuteGradality();
        return LocalDateTime.ofEpochSecond(60 * fromMins, 0, ZoneOffset.UTC);
    }

    private List<HasTimeslot<G>> getShiftsDuring(I time, Collection<? extends HasTimeslot<G>> shifts) {
        return shifts.stream().filter((shift) -> TimeSlotUtils.doTimeslotsIntersect(time.getStartTime(), time
                .getEndTime(), shift
                        .getStartTime(), shift.getEndTime())).collect(Collectors.toList());
    }

    public void addShift(I shift) {
        // TODO: Make better
        setShifts(presenter.getCalendar().getShifts());
    }

    public void removeShift(I shift) {
        // TODO: Make better
        setShifts(presenter.getCalendar().getShifts());
    }

    private void repairShifts(G group) {

    }

    public void setShifts(Collection<I> shifts) {
        this.shifts = shifts;
        shiftDrawables = new ArrayList<>();
        totalSpotSlots = 0;
        groupPos.clear();
        groupEndPos.clear();
        groupContainer.clear();
        groupAddPlane.clear();
        groupIndexOf.clear();
        timeSlotTables.clear();
        groupShifts.clear();
        presenter.getCursorMap().clear();
        allDirty = true;
        visibleDirty = true;
        presenter.setMouseOverDrawable(null);

        HashMap<G, String> colorMap = new HashMap<>();
        int groupIndex = 0;
        for (G group : groups) {
            groupIndexOf.put(group, groupIndex);
            groupPos.put(group, totalSpotSlots);
            final long spotStartPos = totalSpotSlots;
            groupContainer.put(group, new DynamicContainer(() -> new Position(TwoDayViewPresenter.SPOT_NAME_WIDTH,
                    TwoDayViewPresenter.HEADER_HEIGHT
                            + spotStartPos * getGroupHeight())));
            colorMap.put(group, ColorUtils.getColor(colorMap.size()));

            TimeSlotTable<I> timeSlotTable = new TimeSlotTable<>();
            for (I shift : shifts.stream().filter((s) -> s.getGroupId().equals(group)).collect(Collectors.toList())) {
                timeSlotTable.add(shift.getStartTime().toEpochSecond(ZoneOffset.UTC),
                        shift.getEndTime().toEpochSecond(ZoneOffset.UTC), shift);
            }

            List<List<TimeSlotTable.TimeSlot<I>>> grid = timeSlotTable.getTimeSlotsAsGrid();
            timeSlotTables.put(group, timeSlotTable);
            groupShifts.put(group, grid);

            totalSpotSlots += groupShifts.get(group).size() + 1;
            final int spotEndPos = totalSpotSlots;
            groupEndPos.put(group, spotEndPos - 1);
            groupAddPlane.put(group, new DynamicContainer(() -> new Position(TwoDayViewPresenter.SPOT_NAME_WIDTH,
                    TwoDayViewPresenter.HEADER_HEIGHT
                            + getGroupHeight() * (spotEndPos - 1))));

            for (int index = 0; index < grid.size(); index++) {
                for (TimeSlotTable.TimeSlot<I> shift : grid.get(index)) {
                    D drawable = presenter.getConfig().getDrawableProvider().createDrawable(presenter, shift.getData(),
                            index);
                    drawable.setParent(groupContainer.get(group));
                    shiftDrawables.add(drawable);
                }

            }
            groupIndex++;
        }

        timeSlotTableView = new TimeSlotTableView<D, G>(shiftDrawables, groupPos, getViewStartDate(), getViewEndDate());

        for (G spot : groups) {
            presenter.getCursorMap().put(spot, groupEndPos.get(spot));
        }

        presenter.getView().updatePager();
        presenter.draw();
    }

}
