package org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.DynamicContainer;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Position;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeSlotTableView;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlotTable;

/**
 * Internal State of {@link TwoDayView} that external classes should
 * not access or modify.
 *
 * @param <G> Type of the group.
 * @param <I> Type of the shift.
 * @param <D> {@link TimeRowDrawable} used for drawing shifts.
 */
public class TwoDayViewState<G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G, I>> {
    // TODO: Javadoc for methods whose names tell all.

    private TwoDayViewPresenter<G, I, D> presenter;

    /**
     * List of groups that contains the shifts. The order of this list
     * Control the order which groups are drawn.
     */
    private List<G> groupList = new ArrayList<>();

    /**
     * Cached {@link TwoDayViewState#groupList}.indexOf(group).
     */
    private Map<G, Integer> groupIndexMap;

    /**
     * True iff the page change or if {@link TwoDayViewState#allDirty} is true.
     */
    private boolean visibleDirty;

    /**
     * True iff a shift has been added/modified/deleted.
     */
    private boolean allDirty;

    /**
     * Maps each group to its shift grid
     * (the grid has the property that no two shifts in the same
     * row intersect each other).
     */
    private Map<G, List<List<TimeSlotTable.TimeSlot<I>>>> groupShiftMap;

    /**
     * View of the time slot table. 
     */
    private TimeSlotTableView<G, I, D> timeSlotTableView;

    /**
     * Width of the canvas.
     */
    private double screenWidth;

    /**
     * Height of the canvas.
     */
    private double screenHeight;

    /**
     * Width of one minute on the canvas, in pixels.
     */
    private double widthPerMinute;

    /**
     * Height of a row on the canvas, in pixels.
     */
    private double spotHeight;

    /**
     * Total number of rows.
     */
    private int totalSpotSlots;

    /**
     * Epoch time (Used a reference for other dates).
     */
    private LocalDateTime baseDate;

    /**
     * First date visible in the view.
     */
    private LocalDateTime currDay;

    /**
     * {@link TwoDayView#startDateControlScrollbar} "knob" start position.
     */
    private double scrollBarPos;

    /**
     * {@link TwoDayView#startDateControlScrollbar} length.
     */
    private double scrollBarLength;

    /**
     * {@link TwoDayView#startDateControlScrollbar} "knob" length.
     */
    private int scrollBarHandleLength;

    /**
     * Mapping of group to where the first row of the group is drawn.
     */
    private Map<G, DynamicContainer> groupContainerMap = new HashMap<>();

    /**
     * Mapping of group to their empty row (placed after the last row of the group).
     */
    private Map<G, DynamicContainer> groupAddPlaneMap = new HashMap<>();

    public TwoDayViewState(TwoDayViewPresenter<G, I, D> presenter) {
        this.presenter = presenter;
        baseDate = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        currDay = baseDate;
        visibleDirty = true;
        allDirty = true;
        screenWidth = 1;
        screenHeight = 1;
        groupShiftMap = new HashMap<>();
        scrollBarPos = 0;
        scrollBarLength = 1;
        scrollBarHandleLength = 1;
        groupIndexMap = new HashMap<>();
    }

    /**
     * Sets {@link TwoDayViewState#currDay} to the specified date.
     * 
     * @param date The day to set {@link TwoDayViewState#currDay} to.
     */
    public void setDate(LocalDateTime date) {
        visibleDirty = true;
        currDay = date;
        timeSlotTableView.setStartDate(getViewStartDate());
        timeSlotTableView.setEndDate(getViewEndDate());
        presenter.draw();
    }

    public List<G> getGroupList() {
        return Collections.unmodifiableList(groupList);
    }

    public void setGroupList(List<G> groupList) {
        groupIndexMap.clear();
        this.groupList = groupList.stream().sorted((a, b) -> CommonUtils.stringWithIntCompareTo(a.getTitle(), b
                                                                                                               .getTitle()))
                                  .collect(Collectors
                                                     .toList());
        for (int i = 0; i < this.groupList.size(); i++) {
            groupIndexMap.put(this.groupList.get(i), i);
        }
    }

    public double getScrollBarPos() {
        return scrollBarPos;
    }

    public double getScrollBarLength() {
        return scrollBarLength;
    }

    public int getScrollBarHandleLength() {
        return scrollBarHandleLength;
    }

    public void setScrollBarPos(double pos) {
        scrollBarPos = pos;
    }

    public void setScrollBarLength(double length) {
        scrollBarLength = length;
    }

    public void setScrollBarHandleLength(int length) {
        scrollBarHandleLength = length;
    }

    public LocalDateTime getBaseDate() {
        return baseDate;
    }

    public TimeSlotTableView<G, I, D> getTimeSlotTable() {
        if (null == timeSlotTableView) {
            timeSlotTableView = new TimeSlotTableView<>(presenter, groupList, Collections.emptyList(),
                                                        getViewStartDate(),
                                                        getViewEndDate(), presenter.getConfig().getDrawableProvider());
        }
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
        return TwoDayViewPresenter.HEADER_HEIGHT + (getGroupPosMap().get(group) + slot) * getGroupHeight() - getOffsetY();
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
        return groupIndexMap.get(groupId);
    }

    /**
     * Returns how far right, in pixels, the canvas has shifted from {@link TwoDayViewState#baseDate}.
     * @return How far right, in pixels, the canvas has shifted from {@link TwoDayViewState#baseDate}.
     */
    public double getOffsetX() {
        return getLocationOfDate(baseDate.plusSeconds(currDay.toEpochSecond(
                                                                            ZoneOffset.UTC) - baseDate.toEpochSecond(
                                                                                                                     ZoneOffset.UTC)));
    }

    /**
     * Returns how far down, in pixels, the canvas is below the first page.
     * @return How far down, in pixels, the canvas is below the first page.
     */
    public double getOffsetY() {
        return (presenter.getView().getScreenHeight() - TwoDayViewPresenter.HEADER_HEIGHT - spotHeight) * presenter
                                                                                                                   .getPage();
    }

    /**
     * Returns the mapping from group to it first row in {@link TwoDayViewPager#getItemList()}.
     * @return The mapping from group to it first row in {@link TwoDayViewPager#getItemList()}.
     */
    public Map<G, Integer> getGroupPosMap() {
        return timeSlotTableView.getGroupStartPosMap();
    }

    /**
     * Returns the mapping from group to it last row in {@link TwoDayViewPager#getItemList()} + 1.
     * @return The mapping from group to it last row in {@link TwoDayViewPager#getItemList()} + 1.
     */
    public Map<G, Integer> getGroupEndPosMap() {
        return timeSlotTableView.getGroupEndPosMap();
    }

    public Map<G, DynamicContainer> getGroupContainerMap() {
        return groupContainerMap;
    }

    public Map<G, DynamicContainer> getGroupAddPlaneMap() {
        return groupAddPlaneMap;
    }

    /**
     * Returns the x-coordinate of a specified date.
     * @param date The date to get the x-coordinate of.
     * @return The x-coordinate of a specified date.
     */
    public double getLocationOfDate(LocalDateTime date) {
        return ((date.toEpochSecond(ZoneOffset.UTC) - currDay.toEpochSecond(ZoneOffset.UTC)) / 60) * getWidthPerMinute() + TwoDayViewPresenter.SPOT_NAME_WIDTH;

    }

    /**
     * Returns the difference, in days, between {@link TwoDayViewConfig#getHardEndDateBound()} and
     * {@link TwoDayViewConfig#getHardStartateBound()} if both are set, 0 otherwise.
     * @return The difference between {@link TwoDayViewConfig#getHardEndDateBound()} and
     * {@link TwoDayViewConfig#getHardStartateBound()} if both are set, 0 otherwise.
     */
    public double getDaysBetweenEndpoints() {
        if (presenter.getConfig().getHardStartDateBound() != null && presenter.getConfig()
                                                                              .getHardEndDateBound() != null) {
            return (presenter.getConfig().getHardEndDateBound().toEpochSecond(ZoneOffset.UTC) -
                    presenter.getConfig().getHardStartDateBound().toEpochSecond(ZoneOffset.UTC) + 0.0) / TwoDayViewPresenter.SECONDS_PER_DAY;
        }
        return 0;
    }

    /**
     * Returns the difference, in days, between {@link TwoDayViewState#currDay} and 
     * {@link TwoDayViewState#baseDate}.
     * @return The difference, in days, between {@link TwoDayViewState#currDay} and 
     * {@link TwoDayViewState#baseDate}.
     */
    public double getDifferenceFromBaseDate() {
        return (currDay.toEpochSecond(ZoneOffset.UTC) - baseDate.toEpochSecond(ZoneOffset.UTC)) / (TwoDayViewPresenter.SECONDS_PER_DAY + 0.0);
    }

    /**
     * Rounds a specified date to the nearest multiple of {@link TwoDayViewConfig#getEditMinuteGradality()}.
     * @param toRound The date to round.
     * @return toRound rounded to the neared multiple of {@link TwoDayViewConfig#getEditMinuteGradality()}.
     */
    public LocalDateTime roundLocalDateTime(LocalDateTime toRound) {
        long fromMins = Math.round(toRound.toEpochSecond(ZoneOffset.UTC) / (60.0 * presenter.getConfig()
                                                                                            .getEditMinuteGradality())) * presenter.getConfig().getEditMinuteGradality();
        return LocalDateTime.ofEpochSecond(60 * fromMins, 0, ZoneOffset.UTC);
    }

    public void addShift(I shift) {
        timeSlotTableView.addTimeSlot(shift);
    }

    public void updateShift(I oldShift, I newShift) {
        timeSlotTableView.updateTimeSlot(oldShift, newShift);
    }

    public void removeShift(I shift) {
        timeSlotTableView.removeTimeSlot(shift);
    }

    public void removeDrawable(D drawable) {
        timeSlotTableView.removeTimeSlot(drawable);
    }

    public void setShifts(Collection<I> shifts) {
        totalSpotSlots = 0;
        groupContainerMap.clear();
        groupAddPlaneMap.clear();
        groupShiftMap.clear();
        presenter.getCursorIndexMap().clear();
        allDirty = true;
        visibleDirty = true;
        presenter.setMouseOverDrawable(null);
        List<TimeSlotTable<I>> timeSlotTables = new ArrayList<>(groupList.size());

        for (G group : groupList) {
            final long spotStartPos = totalSpotSlots;
            groupContainerMap.put(group, new DynamicContainer(() -> new Position(TwoDayViewPresenter.SPOT_NAME_WIDTH,
                                                                                 TwoDayViewPresenter.HEADER_HEIGHT + spotStartPos * getGroupHeight())));

            TimeSlotTable<I> timeSlotTable = new TimeSlotTable<>();
            for (I shift : shifts.stream().filter((s) -> s.getGroupId().equals(group)).collect(Collectors.toList())) {
                timeSlotTable.add(shift.getStartTime().toEpochSecond(ZoneOffset.UTC),
                                  shift.getEndTime().toEpochSecond(ZoneOffset.UTC), shift);
            }
            timeSlotTables.add(timeSlotTable);
            groupContainerMap.put(group, new DynamicContainer(() -> {
                return new Position(TwoDayViewPresenter.SPOT_NAME_WIDTH, TwoDayViewPresenter.HEADER_HEIGHT + getGroupPosMap().get(group) * presenter.getGroupHeight());
            }));
            groupAddPlaneMap.put(group, new DynamicContainer(() -> {
                return new Position(TwoDayViewPresenter.SPOT_NAME_WIDTH, (getGroupEndPosMap().get(group) + 1) * presenter
                                                                                                                         .getGroupHeight());
            }));
        }

        timeSlotTableView = new TimeSlotTableView<>(presenter, groupList, timeSlotTables, getViewStartDate(),
                                                    getViewEndDate(), presenter.getConfig().getDrawableProvider());

        for (G spot : groupList) {
            presenter.getCursorIndexMap().put(spot, getGroupEndPosMap().get(spot));
        }

        presenter.getView().updatePager();
        presenter.draw();
    }

}
