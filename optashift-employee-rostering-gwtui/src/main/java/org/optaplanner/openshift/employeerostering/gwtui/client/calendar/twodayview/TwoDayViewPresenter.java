package org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import elemental2.dom.MouseEvent;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Calendar;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.CalendarPresenter;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.DateDisplay;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Drawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawableProvider;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

public class TwoDayViewPresenter<G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G, I>>
        implements
        CalendarPresenter<G,
                I> {

    public static final int SECONDS_PER_MINUTE = 60;
    public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60;
    public static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;

    public static final String[] WEEKDAYS = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().weekdaysFull();
    public static final int WEEK_START = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().firstDayOfTheWeek();

    public static final int HEADER_HEIGHT = 64;
    public static final double SPOT_NAME_WIDTH = 200;

    private Calendar<G, I> calendar;
    private TwoDayView<G, I, D> view;
    private TwoDayViewMouseHandler<G, I, D> mouseHandler;
    private TwoDayViewPager<G, I, D> pager;
    private TwoDayViewState<G, I, D> state;
    private TwoDayViewConfig<G, I, D> config;

    private int totalDisplayedSpotSlots;

    private String popupText;
    private Drawable toolBox;

    public TwoDayViewPresenter(Calendar<G, I> calendar, TimeRowDrawableProvider<G, I,
            D> drawableProvider, DateDisplay dateDisplay, TranslationService translator) {
        this.calendar = calendar;
        config = new TwoDayViewConfig<>(this, translator, dateDisplay, drawableProvider);
        state = new TwoDayViewState<>(this);
        pager = new TwoDayViewPager<>(this);
        mouseHandler = new TwoDayViewMouseHandler<>(this);

        totalDisplayedSpotSlots = 10;
        popupText = null;
        toolBox = null;
        view = TwoDayView.create(calendar.getBeanManager(), this);
    }

    // Getter/Setters
    public TwoDayViewState<G, I, D> getState() {
        return state;
    }

    public TwoDayViewConfig<G, I, D> getConfig() {
        return config;
    }

    public TwoDayViewPager<G, I, D> getPager() {
        return pager;
    }

    public TwoDayView<G, I, D> getView() {
        return view;
    }

    public Calendar<G, I> getCalendar() {
        return calendar;
    }

    public double getWidthPerMinute() {
        return state.getWidthPerMinute();
    }

    public double getGroupHeight() {
        return state.getGroupHeight();
    }

    public double getLocationOfDate(LocalDateTime date) {
        return state.getLocationOfDate(date);
    }

    public double getLocationOfGroupSlot(G group, int index) {
        return state.getLocationOfGroupSlot(group, index);
    }

    public int getGroupIndex(G groupId) {
        return state.getGroupIndex(groupId);
    }

    public LocalDateTime roundLocalDateTime(LocalDateTime date) {
        return state.roundLocalDateTime(date);
    }

    public G getSelectedSpot() {
        return mouseHandler.getSelectedSpot();
    }

    public Long getSelectedIndex() {
        return mouseHandler.getSelectedIndex();
    }

    public G getOverSpot() {
        return mouseHandler.getOverSpot();
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
        return mouseHandler.getMouseOverDrawable();
    }

    public void setMouseOverDrawable(D mouseOverDrawable) {
        mouseHandler.setMouseOverDrawable(mouseOverDrawable);
    }

    // Calculated fields getters
    public int getPage() {
        return pager.getPage();
    }

    public boolean isDragging() {
        return mouseHandler.isDragging();
    }

    public boolean isCreating() {
        return mouseHandler.isCreating();
    }

    public Integer getCursorIndex(G spot) {
        return mouseHandler.getCursorIndexMap().get(spot);
    }

    public Map<G, Integer> getCursorIndexMap() {
        return mouseHandler.getCursorIndexMap();
    }

    public void preparePopup(String text) {
        popupText = text;
    }

    public void setPage(int page) {
        pager.setPage(page);
    }

    @Override
    public Set<G> getVisibleGroupSet() {
        return pager.getVisibleGroupSet();
    }

    public int getTotalDisplayedSpotSlots() {
        return totalDisplayedSpotSlots;
    }

    // Mouse Handling
    public LocalDateTime getMouseLocalDateTime() {
        return mouseHandler.getMouseLocalDateTime();
    }

    @Override
    public void onMouseDown(MouseEvent e) {
        mouseHandler.onMouseDown(e);
    }

    @Override
    public void onMouseUp(MouseEvent e) {
        mouseHandler.onMouseUp(e);
    }

    @Override
    public void onMouseMove(MouseEvent e) {
        mouseHandler.onMouseMove(e);
    }

    public double getGlobalMouseX() {
        return mouseHandler.getGlobalMouseX();
    }

    public double getGlobalMouseY() {
        return mouseHandler.getGlobalMouseY();
    }

    public double getLocalMouseX() {
        return mouseHandler.getLocalMouseX();
    }

    public double getLocalMouseY() {
        return mouseHandler.getLocalMouseY();
    }

    public double getDragStartX() {
        return mouseHandler.getDragStartX();
    }

    public double getDragStartY() {
        return mouseHandler.getDragStartY();
    }

    // HasRows/HasData methods

    // View defer
    public void draw() {
        if (null != getHardStartDateBound() && null != getHardEndDateBound()) {
            int daysBetween = (int) ((getHardEndDateBound().toEpochSecond(ZoneOffset.UTC) - getHardStartDateBound()
                    .toEpochSecond(
                            ZoneOffset.UTC)) / (60 * 60 * 24));
            state.setScrollBarLength((daysBetween + 0.0) / getDaysShown());
            state.setScrollBarHandleLength(config.getDaysShown());
            state.setScrollBarPos((state.getViewStartDate().toEpochSecond(ZoneOffset.UTC) -
                    state.getViewStartDate().toEpochSecond(ZoneOffset.UTC) + 0.0) / (SECONDS_PER_DAY * daysBetween));
        } else {
            state.setScrollBarPos(0);
            state.setScrollBarLength(0);
            state.setScrollBarHandleLength(0);
        }
        view.updateScrollBars();
        view.setViewSize(state.getScreenWidth(), state.getScreenHeight());
        view.draw();
    }

    public void setViewSize(double screenWidth, double screenHeight) {
        state.setScreenWidth(screenWidth);
        state.setScreenHeight(screenHeight);
        view.setViewSize(screenWidth, screenHeight);
        state.setWidthPerMinute((view.getScreenWidth() - SPOT_NAME_WIDTH) / (config.getDaysShown() * (SECONDS_PER_DAY
                / SECONDS_PER_MINUTE)));
        state.setGroupHeight((view.getScreenHeight() - HEADER_HEIGHT) / (totalDisplayedSpotSlots + 1));
    }

    @Override
    public HTMLElement getElement() {
        return view.getElement();
    }

    @Override
    public void setDate(LocalDateTime date) {
        state.setDate(date);
    }

    @Override
    public LocalDateTime getViewStartDate() {
        return state.getViewStartDate();
    }

    @Override
    public LocalDateTime getViewEndDate() {
        return state.getViewEndDate();
    }

    @Override
    public LocalDateTime getHardStartDateBound() {
        return config.getHardStartDateBound();
    }

    @Override
    public void setHardStartDateBound(LocalDateTime hardStartDateBound) {
        config.setHardStartDateBound(hardStartDateBound);
    }

    @Override
    public LocalDateTime getHardEndDateBound() {
        return config.getHardEndDateBound();
    }

    @Override
    public void setHardEndDateBound(LocalDateTime hardEndDateBound) {
        config.setHardEndDateBound(hardEndDateBound);
    }

    @Override
    public void addShift(I shift) {
        state.addShift(shift);
    }

    @Override
    public void removeShift(I shift) {
        state.removeShift(shift);
    }

    public void removeDrawable(I shift, D drawable) {
        calendar.getShiftSet().remove(shift);
        state.removeDrawable(drawable);
    }

    @Override
    public void setShifts(Collection<I> shifts) {
        state.setShifts(shifts);
    }

    @Override
    public List<G> getGroupList() {
        return state.getGroupList();
    }

    @Override
    public void setGroupList(List<G> groupList) {
        state.setGroupList(groupList);
    }

    @Override
    public void setScreenHeight(double screenHeight) {
        state.setScreenHeight(screenHeight);
    }

    @Override
    public int getDaysShown() {
        return config.getDaysShown();
    }

    @Override
    public void setDaysShown(int daysShown) {
        config.setDaysShown(daysShown);
    }

    @Override
    public int getEditMinuteGradality() {
        return config.getEditMinuteGradality();
    }

    @Override
    public void setEditMinuteGradality(int editMinuteGradality) {
        config.setEditMinuteGradality(editMinuteGradality);
    }

    @Override
    public int getDisplayMinuteGradality() {
        return config.getDisplayMinuteGradality();
    }

    @Override
    public void setDisplayMinuteGradality(int displayMinuteGradality) {
        config.setDisplayMinuteGradality(displayMinuteGradality);
    }

    @Override
    public void updateShift(I oldShift, I newShift) {
        state.updateShift(oldShift, newShift);
    }

}