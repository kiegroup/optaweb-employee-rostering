package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.rotation;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.GridObject;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl.AbstractHasTimeslotGridObject;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl.Draggability;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl.Resizability;
import org.optaplanner.openshift.employeerostering.shared.common.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.ShiftTemplateView;

@Templated
public class ShiftTemplateGridObject extends AbstractHasTimeslotGridObject<RotationMetadata> implements GridObject<LocalDateTime, RotationMetadata> {

    private ShiftTemplateModel model;
    private ShiftTemplateView shiftTemplateView;

    @Inject
    @DataField("label")
    @Named("span")
    private HTMLElement label;

    @Inject
    private Draggability<LocalDateTime, RotationMetadata> draggability;
    @Inject
    private Resizability<LocalDateTime, RotationMetadata> resizability;

    public void withShiftTemplateModel(ShiftTemplateModel model) {
        this.model = model;
        ShiftTemplateView newShift = new ShiftTemplateView();
        newShift.setId(model.getShiftTemplateView().getId());
        newShift.setRotationEmployeeId(model.getShiftTemplateView().getRotationEmployeeId());
        this.shiftTemplateView = newShift;

        if (getLane() != null) {
            label.innerHTML = (newShift.getRotationEmployeeId() != null) ? new SafeHtmlBuilder().appendEscaped(getLane().getMetadata()
                                                                                                                        .getEmployeeIdToEmployeeMap()
                                                                                                                        .get(newShift.getRotationEmployeeId())
                                                                                                                        .getName()).toSafeHtml().asString() : "Unassigned";
            updatePositionInLane();
        }
    }

    @Override
    public void setStartPositionInScaleUnits(LocalDateTime newStartPosition) {
        updateStartDateTimeWithoutRefresh(newStartPosition);
        if (isAfterMidpoint(newStartPosition)) {
            model.setLaterTwin(this);
        } else {
            model.setEarilerTwin(this);
        }
        model.refreshTwin(this);
    }

    @Override
    public void setEndPositionInScaleUnits(LocalDateTime newEndPosition) {
        updateEndDateTimeWithoutRefresh(newEndPosition);
        model.refreshTwin(this);
    }

    @Override
    public void init(Lane<LocalDateTime, RotationMetadata> lane) {
        updatePositionInLane();
        if (shiftTemplateView != null) {
            label.innerHTML = (shiftTemplateView.getRotationEmployeeId() != null) ? new SafeHtmlBuilder().appendEscaped(getLane().getMetadata()
                                                                                                                                 .getEmployeeIdToEmployeeMap()
                                                                                                                                 .get(shiftTemplateView.getRotationEmployeeId())
                                                                                                                                 .getName()).toSafeHtml().asString() : "Unassigned";
        }
        draggability.applyFor(this, lane.getScale());
        resizability.applyFor(this, lane.getScale());
    }

    private void updatePositionInLane() {
        Long daysInRotation = getDaysInRotation();
        LocalDateTime baseDate = (model.isLaterTwin(this)) ? getLane().getScale().getStartInScaleUnits() : getLane().getScale().getStartInScaleUnits()
                                                                                                                    .minusDays(daysInRotation);
        updateStartDateTimeWithoutRefresh(baseDate.plus(model.getShiftTemplateView().getDurationBetweenRotationStartAndTemplateStart()));
        updateEndDateTimeWithoutRefresh(getStartPositionInScaleUnits().plus(model.getShiftTemplateView().getShiftTemplateDuration()));
    }

    @Override
    public Long getId() {
        return shiftTemplateView.getId();
    }

    protected void updateStartDateTimeWithoutRefresh(LocalDateTime newStartDateTime) {
        shiftTemplateView.setDurationBetweenRotationStartAndTemplateStart(Duration.between(getLane().getScale().getStartInScaleUnits(),
                                                                                           newStartDateTime));
        if (model.isLaterTwin(this)) {
            model.getShiftTemplateView()
                 .setDurationBetweenRotationStartAndTemplateStart(shiftTemplateView.getDurationBetweenRotationStartAndTemplateStart());
        }
    }

    protected void updateEndDateTimeWithoutRefresh(LocalDateTime newEndDateTime) {
        shiftTemplateView.setShiftTemplateDuration(Duration.between(getStartPositionInScaleUnits(),
                                                                    newEndDateTime));

        if (model.isLaterTwin(this)) {
            model.getShiftTemplateView()
                 .setShiftTemplateDuration(shiftTemplateView.getShiftTemplateDuration());
        }
    }

    protected boolean isAfterMidpoint(LocalDateTime dateTime) {
        // Note: Multiply by 12 instead of by 24 since we are doubling it to see if it is past the midpoint
        return getDaysInRotation() < Duration.between(getLane().getScale().getStartInScaleUnits(), dateTime).getSeconds() / 60 / 60 / 12;
    }

    protected Long getDaysInRotation() {
        return Duration.between(getLane().getScale().getStartInScaleUnits(),
                                getLane().getScale().getEndInScaleUnits()).getSeconds() / 60 / 60 / 24;
    }

    protected void reposition() {
        if (getLane() != null) {
            getLane().positionGridObject(this);
        }
    }

    @EventHandler("root")
    private void onClick(@ForEvent("click") MouseEvent e) {
        if (e.shiftKey) {
            getLane().removeGridObject(model);
        }
    }

    @Override
    protected HasTimeslot getTimeslot() {
        return shiftTemplateView;
    }

    @Override
    public void save() {
        // Nothing to save; save is done in batch for Rotation
    }

}
