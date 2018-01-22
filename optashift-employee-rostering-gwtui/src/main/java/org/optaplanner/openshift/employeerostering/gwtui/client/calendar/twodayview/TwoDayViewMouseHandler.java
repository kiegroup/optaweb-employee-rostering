package org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Drawable.PostMouseDownEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;

public class TwoDayViewMouseHandler<G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G, I>> {

    private TwoDayViewPresenter<G, I, D> presenter;

    private HashMap<G, Integer> cursorIndexMap = new HashMap<>();
    private G selectedSpot;
    private Long selectedIndex;
    private G overSpot;
    private D mouseOverDrawable;

    private double mouseX, mouseY;
    private double localMouseX, localMouseY;
    private double dragStartX, dragStartY;

    private boolean isDragging, isCreating;

    public TwoDayViewMouseHandler(TwoDayViewPresenter<G, I, D> presenter) {
        this.presenter = presenter;
        isDragging = false;
        isCreating = false;
        selectedSpot = null;
        selectedIndex = null;
        overSpot = null;
        mouseOverDrawable = null;
    }

    public LocalDateTime getMouseLocalDateTime() {
        try {
            return presenter.getState().getViewStartDate().plusMinutes(Math.round((localMouseX
                    - TwoDayViewPresenter.SPOT_NAME_WIDTH) / presenter.getState().getWidthPerMinute()));
        } catch (DateTimeException e) {
            return presenter.getState().getBaseDate();
        }
    }

    private void handleMouseDown(double eventX, double eventY) {
        double offsetX = presenter.getState().getOffsetX();
        for (G spot : presenter.getState().getGroupAddPlaneMap().keySet()) {
            if (presenter.getState().getGroupContainerMap().get(spot).getGlobalX() < mouseX - offsetX && presenter
                    .getState().getGroupContainerMap()
                    .get(spot)
                    .getGlobalY() < mouseY && mouseY < presenter.getState().getGroupAddPlaneMap().get(spot).getGlobalY()
                            + presenter.getState()
                                    .getGroupHeight()) {
                int index = (int) Math.floor((mouseY - presenter.getState().getGroupContainerMap().get(spot).getGlobalY())
                        / presenter.getState()
                                .getGroupHeight());
                if (null != overSpot) {
                    cursorIndexMap.put(overSpot, presenter.getState().getGroupEndPosMap().get(overSpot));
                }
                selectedSpot = spot;
                overSpot = spot;
                cursorIndexMap.put(overSpot, index);
                isCreating = true;
                selectedIndex = (long) Math.floor((mouseY - presenter.getState().getGroupContainerMap().get(spot)
                        .getGlobalY())
                        / presenter.getState().getGroupHeight());
                break;
            }
        }
    }

    private void handleMouseUp(double eventX, double eventY) {
        if (null != selectedSpot) {
            long fromMins = Math.round((dragStartX - TwoDayViewPresenter.SPOT_NAME_WIDTH - presenter.getState()
                    .getOffsetX())
                    / (presenter.getState().getWidthPerMinute()
                            * presenter.getConfig().getEditMinuteGradality())) * presenter.getConfig()
                                    .getEditMinuteGradality();
            LocalDateTime from = LocalDateTime.ofEpochSecond(60 * fromMins, 0, ZoneOffset.UTC).plusSeconds(
                    presenter.getState().getViewStartDate().toEpochSecond(ZoneOffset.UTC) - presenter.getState()
                            .getBaseDate().toEpochSecond(
                                    ZoneOffset.UTC));
            long toMins = Math.max(0, Math.round((mouseX - TwoDayViewPresenter.SPOT_NAME_WIDTH - presenter.getState()
                    .getOffsetX())
                    / (presenter.getState().getWidthPerMinute()
                            * presenter.getConfig().getEditMinuteGradality()))) * presenter.getConfig()
                                    .getEditMinuteGradality();
            LocalDateTime to = LocalDateTime.ofEpochSecond(60 * toMins, 0, ZoneOffset.UTC).plusSeconds(
                    presenter.getState().getViewStartDate().toEpochSecond(ZoneOffset.UTC) - presenter.getState()
                            .getBaseDate().toEpochSecond(
                                    ZoneOffset.UTC));
            if (to.equals(from)) {
                return;
            } else if (to.isBefore(from)) {
                LocalDateTime tmp = to;
                to = from;
                from = tmp;
            }
            presenter.getCalendar().addShift(selectedSpot, from, to);
        }
    }

    public void onMouseDown(MouseEvent e) {
        localMouseX = presenter.getView().getMouseX(e);
        localMouseY = presenter.getView().getMouseY(e);
        mouseX = localMouseX + presenter.getState().getOffsetX();
        mouseY = localMouseY + presenter.getState().getOffsetY();
        dragStartX = mouseX;
        dragStartY = mouseY;
        isDragging = true;

        PostMouseDownEvent consumed = PostMouseDownEvent.IGNORE;
        if (null != presenter.getToolBox()) {
            consumed = presenter.getToolBox().onMouseDown(e, localMouseX, localMouseY);
            if (PostMouseDownEvent.REMOVE_FOCUS == consumed) {
                isDragging = false;
                presenter.draw();
                return;
            } else {
                presenter.setToolBox(null);
            }
        }
        for (D drawable : CommonUtils.flatten(presenter.getPager().getVisibleItems())) {
            LocalDateTime mouseTime = getMouseLocalDateTime();
            double drawablePos = presenter.getState().getLocationOfGroupSlot(drawable.getGroupId(), drawable
                    .getIndex());

            if (localMouseY >= drawablePos && localMouseY <= drawablePos + presenter.getState().getGroupHeight()) {
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
            mouseOverDrawable.onMouseExit(e, mouseX, mouseY);
            mouseOverDrawable = null;
        } else {
            selectedSpot = mouseOverDrawable.getGroupId();
            selectedIndex = (long) mouseOverDrawable.getIndex();
            overSpot = selectedSpot;
            cursorIndexMap.put(overSpot, selectedIndex.intValue());
        }

        presenter.draw();
    }

    public void onMouseUp(MouseEvent e) {
        localMouseX = presenter.getView().getMouseX(e);
        localMouseY = presenter.getView().getMouseY(e);
        mouseX = localMouseX + presenter.getState().getOffsetX();
        mouseY = localMouseY + presenter.getState().getOffsetY();
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
        cursorIndexMap.put(overSpot, presenter.getState().getGroupEndPosMap().get(overSpot));
        overSpot = null;
        selectedSpot = null;
        selectedIndex = 0L;

        presenter.draw();
    }

    public void onMouseMove(MouseEvent e) {
        localMouseX = presenter.getView().getMouseX(e);
        localMouseY = presenter.getView().getMouseY(e);
        mouseX = localMouseX + presenter.getState().getOffsetX();
        mouseY = localMouseY + presenter.getState().getOffsetY();
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
            if (null != presenter.getToolBox()) {
                if (presenter.getToolBox().onMouseMove(e, localMouseX, localMouseY)) {
                    presenter.draw();
                    return;
                }
            }
            for (D drawable : CommonUtils.flatten(presenter.getPager().getVisibleItems())) {
                LocalDateTime mouseTime = getMouseLocalDateTime();
                double drawablePos = presenter.getState().getLocationOfGroupSlot(drawable.getGroupId(), drawable
                        .getIndex());

                if (localMouseY >= drawablePos && localMouseY <= drawablePos + presenter.getState().getGroupHeight()) {
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

        presenter.draw();
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

    public boolean isDragging() {
        return isDragging;
    }

    public boolean isCreating() {
        return isCreating;
    }

    public Map<G, Integer> getCursorIndexMap() {
        return cursorIndexMap;
    }

    public G getSelectedSpot() {
        return selectedSpot;
    }

    public Long getSelectedIndex() {
        return selectedIndex;
    }

    public G getOverSpot() {
        return overSpot;
    }

    public D getMouseOverDrawable() {
        return mouseOverDrawable;
    }

    public void setMouseOverDrawable(D drawable) {
        mouseOverDrawable = drawable;
    }
}
