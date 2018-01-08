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

public class TwoDayViewState<G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G, I>> {

    private TwoDayViewPresenter<G, I, D> presenter;

    private List<G> groups = new ArrayList<>();
    private Map<G, Integer> groupIndexOf;

    private boolean visibleDirty, allDirty;

    private Map<G, List<List<TimeSlotTable.TimeSlot<I>>>> groupShifts;
    private TimeSlotTableView<G, I, D> timeSlotTableView;

    private double screenWidth, screenHeight;
    private double widthPerMinute, spotHeight;
    private int totalSpotSlots;

    private LocalDateTime baseDate;
    private LocalDateTime currDay;

    private double scrollBarPos;
    private int scrollBarLength;
    private int scrollBarHandleLength;

    private HashMap<G, DynamicContainer> groupContainer = new HashMap<>();
    private HashMap<G, DynamicContainer> groupAddPlane = new HashMap<>();

    public TwoDayViewState(TwoDayViewPresenter<G, I, D> presenter) {
        this.presenter = presenter;
        baseDate = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        currDay = baseDate;
        visibleDirty = true;
        allDirty = true;
        screenWidth = 1;
        screenHeight = 1;
        groupShifts = new HashMap<>();
        scrollBarPos = 0;
        scrollBarLength = 1;
        scrollBarHandleLength = 1;
        groupIndexOf = new HashMap<>();
    }

    public void setDate(LocalDateTime date) {
        visibleDirty = true;
        currDay = date;
        timeSlotTableView.setStartDate(getViewStartDate());
        timeSlotTableView.setEndDate(getViewEndDate());
        presenter.draw();
    }

    public List<G> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public void setGroups(List<G> groups) {
        groupIndexOf.clear();
        this.groups = groups.stream().sorted((a, b) -> CommonUtils.stringWithIntCompareTo(a.getTitle(), b.getTitle()))
                .collect(Collectors
                        .toList());
        for (int i = 0; i < this.groups.size(); i++) {
            groupIndexOf.put(this.groups.get(i), i);
        }
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

    public TimeSlotTableView<G, I, D> getTimeSlotTable() {
        if (null == timeSlotTableView) {
            timeSlotTableView = new TimeSlotTableView<>(presenter, groups, Collections.emptyList(), getViewStartDate(),
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
        return TwoDayViewPresenter.HEADER_HEIGHT + (getGroupPos().get(group) + slot) * getGroupHeight() - getOffsetY();
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
        return getLocationOfDate(baseDate.plusSeconds(currDay.toEpochSecond(
                ZoneOffset.UTC) - baseDate.toEpochSecond(
                        ZoneOffset.UTC)));
    }

    public double getOffsetY() {
        return (presenter.getView().getScreenHeight() - TwoDayViewPresenter.HEADER_HEIGHT - spotHeight) * presenter
                .getPage();
    }

    public Map<G, Integer> getGroupPos() {
        return timeSlotTableView.getGroupStartPos();
    }

    public Map<G, Integer> getGroupEndPos() {
        return timeSlotTableView.getGroupEndPos();
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
        groupContainer.clear();
        groupAddPlane.clear();
        groupShifts.clear();
        presenter.getCursorMap().clear();
        allDirty = true;
        visibleDirty = true;
        presenter.setMouseOverDrawable(null);
        List<TimeSlotTable<I>> timeSlotTables = new ArrayList<>(groups.size());

        for (G group : groups) {
            final long spotStartPos = totalSpotSlots;
            groupContainer.put(group, new DynamicContainer(() -> new Position(TwoDayViewPresenter.SPOT_NAME_WIDTH,
                    TwoDayViewPresenter.HEADER_HEIGHT
                            + spotStartPos * getGroupHeight())));

            TimeSlotTable<I> timeSlotTable = new TimeSlotTable<>();
            for (I shift : shifts.stream().filter((s) -> s.getGroupId().equals(group)).collect(Collectors.toList())) {
                timeSlotTable.add(shift.getStartTime().toEpochSecond(ZoneOffset.UTC),
                        shift.getEndTime().toEpochSecond(ZoneOffset.UTC), shift);
            }
            timeSlotTables.add(timeSlotTable);
            groupContainer.put(group, new DynamicContainer(() -> {
                return new Position(TwoDayViewPresenter.SPOT_NAME_WIDTH, TwoDayViewPresenter.HEADER_HEIGHT
                        + getGroupPos().get(group) * presenter.getGroupHeight());
            }));
            groupAddPlane.put(group, new DynamicContainer(() -> {
                return new Position(TwoDayViewPresenter.SPOT_NAME_WIDTH, (getGroupEndPos().get(group) + 1) * presenter
                        .getGroupHeight());
            }));
        }

        timeSlotTableView = new TimeSlotTableView<>(presenter, groups, timeSlotTables, getViewStartDate(),
                getViewEndDate(), presenter.getConfig().getDrawableProvider());

        for (G spot : groups) {
            presenter.getCursorMap().put(spot, getGroupEndPos().get(spot));
        }

        presenter.getView().updatePager();
        presenter.draw();
    }

}
