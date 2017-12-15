package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.NativeHorizontalScrollbar;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.MouseEvent;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.RangeSlider;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;

import static org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TwoDayViewPresenter.*;

@Templated
public class TwoDayView<G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G>> implements
        IsElement {

    private static TwoDayViewPresenter presenterInstance;

    private static final int H_PAD = 200;

    TwoDayViewPresenter<G, I, D> presenter;

    private static final String BACKGROUND_1 = "#efefef";
    private static final String BACKGROUND_2 = "#e0e0e0";
    private static final String LINE_COLOR = "#000000";

    private NativeHorizontalScrollbar startDateControlScrollbar;
    private RangeSlider daysShownRangeSlider;
    private Span daysShownRangeSliderContainer;

    private Pagination pagination;
    private SimplePager pager;

    @Inject
    @DataField
    Div topPanel;

    @Inject
    @DataField
    Span bottomPanel;

    @Inject
    @DataField
    Div sidePanel;

    @Inject
    @DataField
    HTMLCanvasElement canvas;

    public TwoDayView() {
        presenter = presenterInstance;
    }

    @PostConstruct
    public void init() {
        canvas.draggable = false;
        canvas.style.background = "#FFFFFF";

        canvas.addEventListener("mousedown", (e) -> {
            presenter.onMouseDown((MouseEvent) e);
        });

        canvas.addEventListener("mouseup", (e) -> {
            presenter.onMouseUp((MouseEvent) e);
        });

        canvas.addEventListener("mousemove", (e) -> {
            presenter.onMouseMove((MouseEvent) e);
        });

        Window.addResizeHandler((e) -> {
            canvas.width = e.getWidth() - canvas.offsetLeft - H_PAD;
            canvas.height = e.getHeight() - canvas.offsetTop - 100;
            presenter.update();
            presenter.updateBounds(canvas.width, canvas.height);
        });
        initPanels();
    }

    public void updateBounds() {
        canvas.width = Window.getClientWidth() - canvas.offsetLeft - H_PAD;
        canvas.height = Window.getClientHeight() - canvas.offsetTop - 100;
        presenter.updateBounds(canvas.width, canvas.height);
    }

    public static <G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G>> TwoDayView<G, I, D>
            create(SyncBeanManager beanManager, TwoDayViewPresenter<G, I,
                    D> presenter) {
        presenterInstance = presenter;
        return beanManager.lookupBean(TwoDayView.class).newInstance();
    }

    public HTMLCanvasElement getCanvas() {
        return canvas;
    }

    private void initPanels() {
        pagination = new Pagination();
        pager = new SimplePager();

        VerticalPanel bottomWidgets = new VerticalPanel();

        startDateControlScrollbar = new NativeHorizontalScrollbar();
        startDateControlScrollbar.setWidth("100%");
        startDateControlScrollbar.addScrollHandler((e) -> {
            double pos = (startDateControlScrollbar.getHorizontalScrollPosition()
                    + 0.0) /
                    (startDateControlScrollbar.getScrollWidth());
            long secondsBetween = presenter.getHardEndDateBound().toEpochSecond(ZoneOffset.UTC) - presenter
                    .getHardStartDateBound()
                    .toEpochSecond(ZoneOffset.UTC);
            presenter.setToolBox(null);
            presenter.setDate(presenter.getHardStartDateBound().plusSeconds(Math.round(secondsBetween * pos)));
        });

        daysShownRangeSlider = RangeSlider.create();
        daysShownRangeSlider.setMin("1");
        daysShownRangeSlider.setMax("7");
        daysShownRangeSlider.setStep("1");
        daysShownRangeSlider.setAttribute("orient", "vertical");
        CssResources.INSTANCE.calendar().ensureInjected();
        daysShownRangeSlider.setClassName(CssResources.INSTANCE.calendar().verticalSlider());
        daysShownRangeSlider.setValue(Integer.toString(presenter.getDaysShown()));
        Event.setEventListener(daysShownRangeSlider, (e) -> {
            presenter.setDaysShown(Integer
                    .parseInt(daysShownRangeSlider.getValue()));
        });
        //Work around for there not being a Event.ONINPUT
        Event.sinkEvents(daysShownRangeSlider, ~0);
        daysShownRangeSliderContainer = new Span();
        daysShownRangeSliderContainer.getElement().appendChild(daysShownRangeSlider);
        sidePanel.add(daysShownRangeSliderContainer);

        bottomWidgets.add(startDateControlScrollbar);

        bottomWidgets.add(pagination);

        bottomPanel.add(bottomWidgets);

        pager.setDisplay(presenter);
        pager.setPageSize(presenter.getTotalDisplayedSpotSlots());
        pagination.clear();
    }

    public void updatePager() {
        pagination.rebuild(pager);
    }

    public void draw() {
        presenter.setPage(pager.getPage());
        CanvasRenderingContext2D g = (CanvasRenderingContext2D) (Object) canvas.getContext("2d");
        g.clearRect(0, 0, presenter.getScreenWidth(), presenter.getScreenHeight());
        //updateScrollBar();

        drawTimes(g);
        drawSpots(g);
        drawSpotToCreate(g);
        drawToolBox(g);
        drawPopup(g);
    }

    private void drawSpots(CanvasRenderingContext2D g) {
        if (presenter.getGroups().isEmpty()) {
            return;
        }

        int minSize = 12;//Integer.MAX_VALUE;
        //for (G spot : groups) {
        //    minSize = Math.min(minSize, CanvasUtils.fitTextToBox(g, spot.getTitle(), SPOT_NAME_WIDTH, presenter.getGroupHeight()));
        //}

        g.save();
        g.translate(-presenter.getDifferenceFromBaseDate() * (60 * 24) * presenter.getWidthPerMinute(), 0);
        int index = 0;
        Iterable<Collection<D>> toDraw = presenter.getVisibleItems();
        Set<G> drawnSpots = new HashSet<>();
        HashMap<G, Integer> spotIndex = new HashMap<>();
        int groupIndex = presenter.getGroupIndex(presenter.getGroupPos().keySet().stream().filter((group) -> presenter
                .getGroupEndPos().get(
                        group) >= presenter.getVisibleRange().getStart()).min((a, b) -> presenter.getGroupEndPos().get(
                                a) - presenter.getGroupEndPos().get(b)).orElseGet(() -> presenter
                                        .getGroups().get(
                                                0)));

        spotIndex.put(presenter.getGroups().get(groupIndex), index);
        drawnSpots.add(presenter.getGroups().get(groupIndex));

        for (Collection<D> group : toDraw) {
            if (!group.isEmpty()) {
                G groupId = group.iterator().next().getGroupId();

                for (D drawable : group) {
                    if (groupId.equals(presenter.getSelectedSpot()) && drawable.getIndex() >= presenter.getCursorIndex(
                            groupId)
                            && drawable != presenter.getMouseOverDrawable() && presenter.getGlobalMouseX() != presenter
                                    .getDragStartX()) {
                        drawable.doDrawAt(g, drawable.getGlobalX(), HEADER_HEIGHT + (index + 1) * presenter
                                .getGroupHeight());
                    } else {
                        drawable.doDrawAt(g, drawable.getGlobalX(), HEADER_HEIGHT + index * presenter.getGroupHeight());
                    }
                }
                index++;
            } else {
                index++;
                if (groupIndex < presenter.getGroups().size() && presenter.getVisibleRange().getStart()
                        + index > presenter.getGroupEndPos().getOrDefault(presenter
                                .getGroups().get(groupIndex),
                                presenter.getVisibleRange().getStart() + index)) {
                    groupIndex++;
                    if (groupIndex < presenter.getGroups().size()) {
                        spotIndex.put(presenter.getGroups().get(groupIndex), index);
                        drawnSpots.add(presenter.getGroups().get(groupIndex));
                    }
                }
            }
        }
        g.restore();

        CanvasUtils.setFillColor(g, "#FFFFFF");
        g.fillRect(0, HEADER_HEIGHT, SPOT_NAME_WIDTH, presenter.getScreenHeight() - HEADER_HEIGHT);
        CanvasUtils.setFillColor(g, "#000000");
        CanvasUtils.drawLine(g, SPOT_NAME_WIDTH, HEADER_HEIGHT,
                SPOT_NAME_WIDTH, presenter.getScreenHeight(), 2);

        double textHeight = CanvasUtils.getTextHeight(g, minSize);
        g.font = CanvasUtils.getFont(minSize);

        for (G spot : presenter.getGroups().stream().filter((s) -> drawnSpots.contains(s)).collect(Collectors
                .toList())) {
            int pos = spotIndex.get(spot);
            g.fillText(spot.getTitle(), 0, HEADER_HEIGHT + presenter.getGroupHeight() * pos + textHeight + (presenter
                    .getGroupHeight() - textHeight)
                    / 2);
            CanvasUtils.drawLine(g, 0, HEADER_HEIGHT + presenter.getGroupHeight() * pos, presenter.getScreenWidth(),
                    HEADER_HEIGHT + presenter.getGroupHeight() * pos,
                    2);
        }
    }

    private void drawToolBox(CanvasRenderingContext2D g) {
        if (presenter.getToolBox() != null) {
            presenter.getToolBox().draw(g);
        }
    }

    private void drawPopup(CanvasRenderingContext2D g) {
        if (null != presenter.getPopupText()) {
            g.font = CanvasUtils.getFont(12);
            double[] preferredSize = CanvasUtils.getPreferredBoxSizeForText(g, presenter.getPopupText(), 12);
            g.strokeRect(presenter.getLocalMouseX() - preferredSize[0], presenter.getLocalMouseY() - preferredSize[1],
                    preferredSize[0],
                    preferredSize[1]);
            CanvasUtils.setFillColor(g, "#B18800");
            g.fillRect(presenter.getLocalMouseX() - preferredSize[0], presenter.getLocalMouseY() - preferredSize[1],
                    preferredSize[0],
                    preferredSize[1]);
            CanvasUtils.setFillColor(g, "#000000");
            CanvasUtils.drawTextInBox(g, presenter.getPopupText(), presenter.getLocalMouseX() - preferredSize[0],
                    presenter
                            .getLocalMouseY()
                            - preferredSize[1], preferredSize[0], preferredSize[1]);
            presenter.setPopupText(null);
        }
    }

    private void drawSpotToCreate(CanvasRenderingContext2D g) {
        if (presenter.isCreating()) {
            CanvasUtils.setFillColor(g, "#00FF00");
            long fromMins = Math.round((presenter.getDragStartX() - SPOT_NAME_WIDTH - presenter.getOffsetX())
                    / (presenter.getWidthPerMinute()
                            * presenter.getEditMinuteGradality())) * presenter.getEditMinuteGradality();
            LocalDateTime from = LocalDateTime.ofEpochSecond(60 * fromMins, 0, ZoneOffset.UTC).plusSeconds(
                    presenter.getViewStartDate().toEpochSecond(ZoneOffset.UTC) - presenter.getBaseDate().toEpochSecond(
                            ZoneOffset.UTC));
            long toMins = Math.max(0, Math.round((presenter.getGlobalMouseX() - SPOT_NAME_WIDTH - presenter
                    .getOffsetX()) / (presenter
                            .getWidthPerMinute()
                            * presenter.getEditMinuteGradality()))) * presenter.getEditMinuteGradality();
            LocalDateTime to = LocalDateTime.ofEpochSecond(60 * toMins, 0, ZoneOffset.UTC).plusSeconds(
                    presenter.getViewStartDate().toEpochSecond(ZoneOffset.UTC) - presenter.getViewStartDate()
                            .toEpochSecond(
                                    ZoneOffset.UTC));
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
            presenter.preparePopup(timeslot.toString());
            g.fillRect(presenter.getDragStartX() - presenter.getOffsetX(), presenter.getGroupContainer().get(presenter
                    .getSelectedSpot()).getGlobalY()
                    + presenter
                            .getGroupHeight()
                            * presenter.getSelectedIndex() - presenter.getOffsetY(), (toMins - fromMins) * presenter
                                    .getWidthPerMinute(),
                    presenter.getGroupHeight());
        }
    }

    private void drawTimes(CanvasRenderingContext2D g) {
        CanvasUtils.setFillColor(g, "#000000");
        String week = presenter.getDateFormat().format(presenter.getViewStartDate(), WEEK_START, presenter
                .getTranslator());
        int textSize = CanvasUtils.fitTextToBox(g, week, SPOT_NAME_WIDTH, HEADER_HEIGHT / 2);
        for (String day : WEEKDAYS) {
            textSize = Math.min(textSize, CanvasUtils.fitTextToBox(g, day, 24 * 60 * presenter.getWidthPerMinute(),
                    HEADER_HEIGHT / 2));
        }
        g.font = CanvasUtils.getFont(textSize);
        int offset = (presenter.getDateFormat() != DateDisplay.WEEKS_FROM_EPOCH) ? 0 : LocalDateTime.ofEpochSecond(0, 0,
                ZoneOffset.UTC).getDayOfWeek().getValue();

        double drawOffset = presenter.getWidthPerMinute() * 24 * ((presenter.getViewStartDate().toEpochSecond(
                ZoneOffset.UTC) -
                presenter.getViewStartDate().toLocalDate().atTime(0, 0).toEpochSecond(ZoneOffset.UTC)) / (60 * 24.0));
        for (int x = 0; x < presenter.getDaysShown() + 1; x++) {
            g.fillText(WEEKDAYS[(int) (Math.abs((WEEK_START + x + presenter.getViewStartDate().getDayOfWeek().getValue()
                    - 1 + offset))
                    % 7)],
                    SPOT_NAME_WIDTH + (24 * x) * 60 * presenter.getWidthPerMinute() - drawOffset, HEADER_HEIGHT / 2);
            CanvasUtils.drawLine(g, SPOT_NAME_WIDTH + (24 * x) * 60 * presenter.getWidthPerMinute() - drawOffset, 0,
                    SPOT_NAME_WIDTH
                            + (24 * x) * 60
                                    * presenter.getWidthPerMinute() - drawOffset, presenter.getScreenHeight(), 2);
        }
        for (int x = 0; x < (presenter.getScreenWidth() / 2 * (presenter.getWidthPerMinute() * presenter
                .getDisplayMinuteGradality())); x++) {
            if (x % 2 == 0) {
                CanvasUtils.setFillColor(g, BACKGROUND_1);
            } else {
                CanvasUtils.setFillColor(g, BACKGROUND_2);
            }
            g.fillRect(SPOT_NAME_WIDTH + x * presenter.getWidthPerMinute() * presenter.getDisplayMinuteGradality()
                    - drawOffset, HEADER_HEIGHT,
                    SPOT_NAME_WIDTH
                            + (x + 1)
                                    * presenter.getWidthPerMinute() * presenter.getDisplayMinuteGradality(), presenter
                                            .getScreenHeight() - HEADER_HEIGHT);
            CanvasUtils.drawLine(g, SPOT_NAME_WIDTH + x * presenter.getWidthPerMinute() * presenter
                    .getDisplayMinuteGradality()
                    - drawOffset,
                    HEADER_HEIGHT,
                    SPOT_NAME_WIDTH + x * presenter.getWidthPerMinute() * presenter.getDisplayMinuteGradality()
                            - drawOffset, presenter.getScreenHeight(), 1);
        }

        CanvasUtils.setFillColor(g, "#FFFFFF");
        g.fillRect(0, 0, SPOT_NAME_WIDTH, HEADER_HEIGHT);
        CanvasUtils.setFillColor(g, "#000000");
        g.fillText(week, 0, HEADER_HEIGHT / 2);
        CanvasUtils.drawLine(g, SPOT_NAME_WIDTH, 0, SPOT_NAME_WIDTH, presenter.getScreenHeight(), 2);
    }

    public void updateScrollBars() {
        double oldPos = (startDateControlScrollbar.getHorizontalScrollPosition() + 0.0) / startDateControlScrollbar
                .getMaximumHorizontalScrollPosition();
        startDateControlScrollbar.setWidth(Math.round(presenter.getScreenWidth()) + "px");
        daysShownRangeSliderContainer.setHeight(Math.round(presenter.getScreenHeight()) + "px");
        daysShownRangeSlider.setAttribute("style", "height:" + Math.round(presenter.getScreenHeight()) + "px;");

        startDateControlScrollbar.setScrollWidth((int) Math.round(presenter.getScreenWidth() * presenter
                .getScrollBarLength()));
        startDateControlScrollbar.setHorizontalScrollPosition((int) Math.round(oldPos * startDateControlScrollbar
                .getMaximumHorizontalScrollPosition()));
    }
}
