package org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.NativeHorizontalScrollbar;
import com.google.gwt.user.client.ui.VerticalPanel;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.MouseEvent;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.DateDisplay;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.RangeSlider;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;

import static org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter.HEADER_HEIGHT;
import static org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter.SPOT_NAME_WIDTH;
import static org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter.WEEKDAYS;
import static org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter.WEEK_START;

@Templated
/**
 * Widget that draws the shifts. Contains only drawing/widget logic.
 *
 * @param <G> Type of the group.
 * @param <I> Type of the shift.
 * @param <D> {@link TimeRowDrawable} used for drawing shifts.
 */
public class TwoDayView<G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G, I>> implements
                       IsElement {

    /**
     * Presenter to use when constructing an instance.
     */
    @SuppressWarnings("rawtypes")
    private static TwoDayViewPresenter presenterInstance;

    /**
     * Presenter; contains logic and parameters
     */
    TwoDayViewPresenter<G, I, D> presenter;

    // TODO: Move into CSS file
    /**
     * Color of "even" time stripes. 
     */
    private static final String BACKGROUND_1 = "#efefef";
    /**
     * Color of "odd" time stripes.
     */
    private static final String BACKGROUND_2 = "#e0e0e0";

    /**
     * Scroll bar that controls the date being displayed by the view.
     */
    private NativeHorizontalScrollbar startDateControlScrollbar;

    /**
     * Range (discrete scroll bar) that controls the "zoom" level
     * (each step is a day).
     */
    private RangeSlider daysShownRangeSlider;

    // TODO: Do we still need this?
    /**
     * Contains {@link TwoDayView#daysShownRangeSlider}.
     */
    private Span daysShownRangeSliderContainer;

    /**
     * Press to zoom in. Right of {@link TwoDayView#daysShownRangeSlider}.
     */
    private Button zoomIn;

    /**
     * Press to zoom out. Left of {@link TwoDayView#daysShownRangeSlider}.
     */
    private Button zoomOut;

    /**
     * Handles paging.
     */
    private Pagination pagination;

    /**
     * Pager widget (interacts with {@link TwoDayView#pagination}.
     */
    private SimplePager pager;

    /**
     * Height of {@link TwoDayView#daysShownRangeSlider}. Used to insert an invisible button
     * the size of the scroll bar above the view so the view does not overlap the scroll bar.
     */
    private static final int ZOOM_BAR_PAD = 30;

    @Inject
    @DataField
    /**
     * Content above the view.
     */
    Div topPanel;

    // TODO: is this right? Shouldn't the sidePanel be a Span and the bottomPanel
    // be a Div?
    @Inject
    @DataField
    /**
     * Content below the view.
     */
    Span bottomPanel;

    @Inject
    @DataField
    /**
     * Content to the right of the view.
     */
    Div sidePanel;

    @Inject
    @DataField
    /**
     * The view.
     */
    HTMLCanvasElement canvas;

    @Inject
    @DataField
    /**
     * Buffer for the view.
     */
    HTMLCanvasElement buffer;

    // TODO: Find out how to inject the @Dependent presenter...
    // (independent from the Errai framework, if possible)
    @SuppressWarnings("unchecked")
    public TwoDayView() {
        presenter = presenterInstance;
    }

    /**
     * Constructs an instance of TwoDayView.
     * 
     * @param beanManager Errai bean manager.
     * @param presenter Presenter to use.
     * @return A new instance of TwoDayView using the given presenter.
     */
    @SuppressWarnings("unchecked")
    public static <G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G, I>> TwoDayView<G, I, D> create(SyncBeanManager beanManager, TwoDayViewPresenter<G, I, D> presenter) {
        presenterInstance = presenter;
        return beanManager.lookupBean(TwoDayView.class).newInstance();
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

        initPanels();
    }

    /**
     * Creates and initializes the widgets above, to the right of, and below the canvas.
     */
    private void initPanels() {
        pagination = new Pagination();
        pager = new SimplePager();
        VerticalPanel bottomWidgets = new VerticalPanel();

        startDateControlScrollbar = new NativeHorizontalScrollbar();
        startDateControlScrollbar.setWidth("100%");
        startDateControlScrollbar.addScrollHandler((e) -> {
            double pos = (startDateControlScrollbar.getHorizontalScrollPosition() + 0.0) /
                         (startDateControlScrollbar.getScrollWidth());
            long secondsBetween = presenter.getHardEndDateBound().toEpochSecond(ZoneOffset.UTC) - presenter
                                                                                                           .getHardStartDateBound()
                                                                                                           .toEpochSecond(ZoneOffset.UTC);
            presenter.setToolBox(null);
            presenter.getCalendar().setDate(presenter.getHardStartDateBound().plusSeconds(Math.round(secondsBetween * pos)));
        });

        daysShownRangeSlider = RangeSlider.create();
        daysShownRangeSlider.setMin("1");
        daysShownRangeSlider.setMax("7");
        daysShownRangeSlider.setStep("1");
        CssResources.INSTANCE.calendar().ensureInjected();
        daysShownRangeSlider.setClassName(CssResources.INSTANCE.calendar().verticalSlider());
        daysShownRangeSlider.setValue(Integer.toString(presenter.getDaysShown()));
        Event.setEventListener(daysShownRangeSlider, (e) -> {
            presenter.getCalendar().setDaysShown(Integer
                                                        .parseInt(daysShownRangeSlider.getValue()));
        });
        //Work around for there not being a Event.ONINPUT
        Event.sinkEvents(daysShownRangeSlider, ~0);
        daysShownRangeSliderContainer = new Span();
        daysShownRangeSliderContainer.getElement().appendChild(daysShownRangeSlider);

        zoomIn = new Button();
        zoomOut = new Button();

        zoomIn.addClickHandler((e) -> {
            daysShownRangeSlider.setValue("" + (Integer.parseInt(daysShownRangeSlider
                                                                                     .getValue()) - 1));
            presenter.getCalendar().setDaysShown(Integer
                                                        .parseInt(daysShownRangeSlider.getValue()));
        });
        zoomOut.addClickHandler((e) -> {
            daysShownRangeSlider.setValue("" + (Integer.parseInt(daysShownRangeSlider
                                                                                     .getValue()) + 1));
            presenter.getCalendar().setDaysShown(Integer.parseInt(daysShownRangeSlider.getValue()));
        });

        zoomIn.setStylePrimaryName(CssResources.INSTANCE.calendar().sliderButton());
        Span zoomInIcon = new Span();
        zoomInIcon.getElement().setAttribute("class", "glyphicon glyphicon-zoom-in");
        zoomIn.add(zoomInIcon);

        zoomOut.setStylePrimaryName(CssResources.INSTANCE.calendar().sliderButton());
        Span zoomOutIcon = new Span();
        zoomOutIcon.getElement().setAttribute("class", "glyphicon glyphicon-zoom-out");
        zoomOut.add(zoomOutIcon);

        topPanel.add(zoomOut);
        topPanel.add(daysShownRangeSliderContainer);
        topPanel.add(zoomIn);

        // Padding so daysShownRangeSliderContainer does not intersect the canvas
        // (Since they are absolute positioned widgets)
        Button test = new Button();
        test.getElement().setAttribute("style", "background: transparent;" +
                                                " border: none !important;" +
                                                " font-size:0; height:" + ZOOM_BAR_PAD + "px;");
        topPanel.add(test);

        bottomWidgets.add(startDateControlScrollbar);

        bottomWidgets.add(pagination);

        bottomPanel.add(bottomWidgets);

        pager.setDisplay(presenter.getPager());
        pager.setPageSize(presenter.getTotalDisplayedSpotSlots());
        pagination.clear();
    }

    /**
     * Set the size of the entire widget (not just the canvas).
     * 
     * @param screenWidth The new width of the widget.
     * @param screenHeight The new height of the widget.
     */
    public void setViewSize(double screenWidth, double screenHeight) {
        canvas.width = screenWidth - canvas.offsetLeft;
        canvas.height = screenHeight - zoomIn.getOffsetHeight() - canvas.offsetTop;
        buffer.width = canvas.width;
        buffer.height = canvas.height;
    }

    /**
     * Returns the width of the canvas.
     * @return The width of the canvas.
     */
    public double getScreenWidth() {
        return canvas.width;
    }

    /**
     * Returns the height of the canvas.
     * @return The height of the canvas.
     */
    public double getScreenHeight() {
        return canvas.height;
    }

    /**
     * Returns the x-coordinate of the mouse on canvas.
     * @param e Mouse event to convert x-coordinate of.
     * @return x-coordinate of the mouse on canvas.
     */
    public double getMouseX(MouseEvent e) {
        return CanvasUtils.getCanvasX(getCanvas(), e);
    }

    /**
     * Returns the y-coordinate of the mouse on canvas.
     * @param e Mouse event to convert y-coordinate of.
     * @return y-coordinate of the mouse on canvas.
     */
    public double getMouseY(MouseEvent e) {
        return CanvasUtils.getCanvasY(getCanvas(), e);
    }

    /**
     * Returns the canvas.
     * @return The canvas.
     */
    public HTMLCanvasElement getCanvas() {
        return canvas;
    }

    /**
     * Rebuilds the pager. Must be called when rows are added/removed.
     */
    public void updatePager() {
        pagination.rebuild(pager);
    }

    /**
     * Draws 
     */
    public void draw() {
        // Call this to ensure the page used by logic is correct
        presenter.setPage(pager.getPage());

        // Draw the presenter to the buffer
        CanvasRenderingContext2D g = (CanvasRenderingContext2D) (Object) buffer.getContext("2d");
        g.clearRect(0, 0, getScreenWidth(), getScreenHeight());

        drawTimes(g);
        drawSpots(g);
        drawSpotToCreate(g);
        drawToolBox(g);
        drawPopup(g);

        // Copy the buffer to the canvas
        CanvasRenderingContext2D d = (CanvasRenderingContext2D) (Object) canvas.getContext("2d");
        d.clearRect(0, 0, getScreenWidth(), getScreenHeight());
        d.drawImage(buffer, 0, 0);
    }

    /**
     * Draws the visible shifts and group titles.
     * 
     * @param g Where to draw to.
     */
    private void drawSpots(CanvasRenderingContext2D g) {
        if (presenter.getGroupList().isEmpty()) {
            return;
        }

        int minSize = Integer.MAX_VALUE;

        // Get the largest font size such that no group's title is larger than its box
        for (G spot : presenter.getVisibleGroupSet()) {
            minSize = Math.min(minSize, CanvasUtils.fitTextToBox(g, spot.getTitle(),
                                                                 SPOT_NAME_WIDTH, presenter.getGroupHeight()));
        }
        int index = 0;
        Iterable<Collection<D>> toDraw = presenter.getPager().getVisibleItems();
        int startGroupIndex = presenter.getState().getGroupIndex(presenter.getPager().getFirstVisibleGroup());
        int groupIndex = startGroupIndex;

        for (Collection<D> group : toDraw) {
            // Empty group => next row belongs to a different group 
            if (!group.isEmpty()) {
                G groupId = group.iterator().next().getGroupId();
                for (D drawable : group) {
                    double xPos = presenter.getLocationOfDate(drawable.getStartTime());
                    // Check if this row is in the group where a shift is being modified/added, and if
                    // it is below/at the same row as it. If so, move the row down.
                    if (groupId.equals(presenter.getSelectedSpot()) && drawable.getIndex() >= presenter.getCursorIndex(groupId) && drawable != presenter.getMouseOverDrawable() && presenter.getGlobalMouseX() != presenter
                                                                                                                                                                                                                           .getDragStartX()) {
                        drawable.doDrawAt(g, xPos, HEADER_HEIGHT + (index + 1) * presenter.getState()
                                                                                          .getGroupHeight());
                    } else {
                        drawable.doDrawAt(g, xPos, HEADER_HEIGHT + index * presenter.getState()
                                                                                    .getGroupHeight());
                    }
                }
                index++;
            } else {
                index++;
                // Check if the next group is visible.
                if (groupIndex < presenter.getGroupList().size() && presenter.getPager().getVisibleRange().getStart() + index > presenter.getState().getGroupEndPosMap().getOrDefault(presenter
                                                                                                                                                                                               .getGroupList().get(
                                                                                                                                                                                                                   groupIndex),
                                                                                                                                                                                      presenter.getPager().getVisibleRange()
                                                                                                                                                                                               .getStart() + index)) {
                    groupIndex++;
                }
            }
        }

        // TODO: Move into CSS file
        CanvasUtils.setFillColor(g, "#FFFFFF");// Background color of group titles.
        g.fillRect(0, HEADER_HEIGHT, SPOT_NAME_WIDTH, getScreenHeight() - HEADER_HEIGHT);
        CanvasUtils.setFillColor(g, "#000000");
        CanvasUtils.drawLine(g, SPOT_NAME_WIDTH, HEADER_HEIGHT,
                             SPOT_NAME_WIDTH, getScreenHeight(), 2);

        double textHeight = CanvasUtils.getTextHeight(g, minSize);
        g.font = CanvasUtils.getFont(minSize);

        for (G spot : presenter.getGroupList().subList(startGroupIndex, Math.min(presenter.getGroupList().size(),
                                                                                 groupIndex + 1))) {
            int pos = presenter.getState().getGroupPosMap().get(spot) - presenter.getPager().getVisibleRange()
                                                                                 .getStart();
            g.fillText(spot.getTitle(), 0, HEADER_HEIGHT + presenter.getState().getGroupHeight() * pos + textHeight + (presenter.getState()
                                                                                                                                .getGroupHeight() - textHeight) / 2);
            CanvasUtils.drawLine(g, 0, HEADER_HEIGHT + presenter.getState().getGroupHeight() * pos, getScreenWidth(),
                                 HEADER_HEIGHT + presenter.getState().getGroupHeight() * pos,
                                 2);
        }
    }

    /**
     * Draws the tool box (widget that appears when you click a shift)
     * 
     * @param g Where to draw the tool box to.
     */
    private void drawToolBox(CanvasRenderingContext2D g) {
        if (presenter.getToolBox() != null) {
            presenter.getToolBox().draw(g);
        }
    }

    /**
     * Draws the tool tip that appears over shifts when mouse is over them.
     * 
     * @param g Where to draw the tool tip to.
     */
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
                                               .getLocalMouseY() - preferredSize[1], preferredSize[0], preferredSize[1]);
            presenter.setPopupText(null);
        }
    }

    /**
     * Draws a spot being created.
     * 
     * @param g Where to draw the spot being created to.
     */
    private void drawSpotToCreate(CanvasRenderingContext2D g) {
        if (presenter.isCreating()) {
            CanvasUtils.setFillColor(g, "#00FF00");
            long fromMins = Math.round((presenter.getDragStartX() - SPOT_NAME_WIDTH - presenter.getState().getOffsetX()) / (presenter.getState().getWidthPerMinute() * presenter.getEditMinuteGradality())) * presenter
                                                                                                                                                                                                                       .getEditMinuteGradality();
            LocalDateTime from = LocalDateTime.ofEpochSecond(60 * fromMins, 0, ZoneOffset.UTC).plusSeconds(
                                                                                                           presenter.getViewStartDate().toEpochSecond(ZoneOffset.UTC) - presenter.getState().getBaseDate()
                                                                                                                                                                                 .toEpochSecond(
                                                                                                                                                                                                ZoneOffset.UTC));
            long toMins = Math.max(0, Math.round((presenter.getGlobalMouseX() - SPOT_NAME_WIDTH - presenter.getState()
                                                                                                           .getOffsetX()) / (presenter.getState()
                                                                                                                                      .getWidthPerMinute() * presenter.getEditMinuteGradality()))) * presenter
                                                                                                                                                                                                              .getEditMinuteGradality();
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
            g.fillRect(presenter.getDragStartX() - presenter.getState().getOffsetX(), presenter.getState()
                                                                                               .getGroupContainerMap().get(presenter
                                                                                                                                    .getSelectedSpot()).getGlobalY() + presenter.getState()
                                                                                                                                                                                .getGroupHeight() * presenter
                                                                                                                                                                                                             .getSelectedIndex() -
                                                                                      presenter.getState().getOffsetY(), (toMins - fromMins) * presenter.getState()
                                                                                                                                                        .getWidthPerMinute(),
                       presenter.getState().getGroupHeight());
        }
    }

    /**
     * Draws the days of the week, time stripes, times and the date.
     * @param g Where to draw the week, time stripes, times and date to.
     */
    private void drawTimes(CanvasRenderingContext2D g) {
        CanvasUtils.setFillColor(g, "#000000");
        String week = presenter.getConfig().getDateFormat().format(presenter.getViewStartDate(), WEEK_START, presenter
                                                                                                                      .getConfig()
                                                                                                                      .getTranslator());
        int whitespacePadding = 10;
        int textSize = CanvasUtils.fitTextToBox(g, week, SPOT_NAME_WIDTH - whitespacePadding, HEADER_HEIGHT / 2);

        // Find max font size such that no Weekday/Date is larger than its box
        for (String day : WEEKDAYS) {
            textSize = Math.min(textSize, CanvasUtils.fitTextToBox(g, day, 24 * 60 * presenter.getState()
                                                                                              .getWidthPerMinute() - whitespacePadding,
                                                                   HEADER_HEIGHT / 2));
        }
        g.font = CanvasUtils.getFont(textSize);
        int offset = (presenter.getConfig().getDateFormat() != DateDisplay.WEEKS_FROM_EPOCH) ? 0 : LocalDateTime
                                                                                                                .ofEpochSecond(0, 0,
                                                                                                                               ZoneOffset.UTC).getDayOfWeek().getValue();

        double drawOffset = presenter.getState().getWidthPerMinute() * 24 * ((presenter.getViewStartDate()
                                                                                       .toEpochSecond(
                                                                                                      ZoneOffset.UTC) -
                                                                              presenter.getViewStartDate().toLocalDate().atTime(0, 0).toEpochSecond(ZoneOffset.UTC)) / (60 * 24.0));
        for (int x = 0; x < presenter.getDaysShown() + 1; x++) {
            g.fillText(WEEKDAYS[(int) (Math.abs((WEEK_START + x + presenter.getViewStartDate().getDayOfWeek().getValue() - 1 + offset)) % 7)],
                       SPOT_NAME_WIDTH + (24 * x) * 60 * presenter.getState().getWidthPerMinute() - drawOffset + whitespacePadding,
                       HEADER_HEIGHT / 2);
            CanvasUtils.drawLine(g, SPOT_NAME_WIDTH + (24 * x) * 60 * presenter.getState().getWidthPerMinute() - drawOffset, 0,
                                 SPOT_NAME_WIDTH + (24 * x) * 60 * presenter.getState().getWidthPerMinute() - drawOffset, getScreenHeight(), 2);
        }
        for (int x = 0; x < (getScreenWidth() / 2 * (presenter.getState().getWidthPerMinute() * presenter
                                                                                                         .getDisplayMinuteGradality())); x++) {
            if (x % 2 == 0) {
                CanvasUtils.setFillColor(g, BACKGROUND_1);
            } else {
                CanvasUtils.setFillColor(g, BACKGROUND_2);
            }
            g.fillRect(SPOT_NAME_WIDTH + x * presenter.getState().getWidthPerMinute() * presenter
                                                                                                 .getDisplayMinuteGradality() - drawOffset, HEADER_HEIGHT,
                       SPOT_NAME_WIDTH + (x + 1) * presenter.getState().getWidthPerMinute() * presenter.getDisplayMinuteGradality(),
                       presenter.getState()
                                .getScreenHeight() - HEADER_HEIGHT);
            CanvasUtils.drawLine(g, SPOT_NAME_WIDTH + x * presenter.getState().getWidthPerMinute() * presenter
                                                                                                              .getDisplayMinuteGradality() - drawOffset,
                                 HEADER_HEIGHT,
                                 SPOT_NAME_WIDTH + x * presenter.getState().getWidthPerMinute() * presenter
                                                                                                           .getDisplayMinuteGradality() - drawOffset, getScreenHeight(), 1);
        }

        CanvasUtils.setFillColor(g, "#FFFFFF");
        g.fillRect(0, 0, SPOT_NAME_WIDTH, HEADER_HEIGHT);
        CanvasUtils.setFillColor(g, "#000000");
        g.fillText(week, 0, HEADER_HEIGHT / 2);
        CanvasUtils.drawLine(g, SPOT_NAME_WIDTH, 0, SPOT_NAME_WIDTH, getScreenHeight(), 2);
    }

    /**
     * Updates the size and position of the scroll bars.
     */
    public void updateScrollBars() {
        double oldPos = (startDateControlScrollbar.getHorizontalScrollPosition() + 0.0) / startDateControlScrollbar
                                                                                                                   .getMaximumHorizontalScrollPosition();
        startDateControlScrollbar.setWidth(Math.round(canvas.width) + "px");
        daysShownRangeSliderContainer.setWidth(CommonUtils.roundToNearestMultipleOf(canvas.width / 4, 10) + "px");

        double sideBarLength = canvas.width * 0.25 - zoomIn.getOffsetWidth() - zoomOut.getOffsetWidth();
        zoomOut.getElement().setAttribute("style", "left: " + CommonUtils.roundToNearestMultipleOf(0.75 * canvas.width - zoomOut.getOffsetWidth(), 1) + "px");
        daysShownRangeSlider.setAttribute("style", "width:" + CommonUtils.roundToNearestMultipleOf(sideBarLength, 10) + "px; left:" + CommonUtils.roundToNearestMultipleOf(0.75 * canvas.width, 1) + "px");
        zoomIn.getElement().setAttribute("style", "left: " + CommonUtils.roundToNearestMultipleOf(0.75 * canvas.width + sideBarLength, 1) + "px");

        startDateControlScrollbar.setScrollWidth((int) Math.round(canvas.width * presenter.getState()
                                                                                          .getScrollBarLength()));
        startDateControlScrollbar.setHorizontalScrollPosition((int) Math.round(oldPos * startDateControlScrollbar
                                                                                                                 .getMaximumHorizontalScrollPosition()));
    }
}
