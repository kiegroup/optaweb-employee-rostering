package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.employeeroster;

import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.SingleGridObject;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl.AbstractHasTimeslotGridObject;
import org.optaplanner.openshift.employeerostering.shared.common.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;

// TODO: Use this as a base class for SpotRoster ShiftGridObject
@Templated
public class ShiftGridObject extends AbstractHasTimeslotGridObject<EmployeeRosterMetadata> implements SingleGridObject<LocalDateTime, EmployeeRosterMetadata> {

    @Inject
    @DataField("label")
    @Named("span")
    private HTMLElement label;

    private ShiftView shiftView;

    public ShiftView getShiftView() {
        return shiftView;
    }

    @Override
    public void setStartPositionInScaleUnits(LocalDateTime newStartPosition) {
        shiftView.setStartDateTime(newStartPosition);
    }

    @Override
    public void setEndPositionInScaleUnits(LocalDateTime newEndPosition) {
        shiftView.setEndDateTime(newEndPosition);
    }

    public ShiftGridObject withShiftView(ShiftView shiftView) {
        this.shiftView = shiftView;
        refresh();
        return this;
    }

    @Override
    public Long getId() {
        return shiftView.getId();
    }

    private void refresh() {
        if (getLane() != null) {
            getLane().positionGridObject(this);
            label.innerHTML = new SafeHtmlBuilder().appendEscaped(getLane().getMetadata()
                    .getSpotIdToSpotMap().get(shiftView.getSpotId()).getName())
                    .toSafeHtml().asString();
            RosterState rosterState = getLane().getMetadata().getRosterState();
            setClassProperty("historic", rosterState.isHistoric(shiftView));
            setClassProperty("published", rosterState.isPublished(shiftView));
            setClassProperty("draft", rosterState.isDraft(shiftView));
        }
    }

    @Override
    protected HasTimeslot getTimeslot() {
        return shiftView;
    }

    @Override
    protected void init(Lane<LocalDateTime, EmployeeRosterMetadata> lane) {
        refresh();
    }

    // Note: Should not be called, since shifts are modified in SpotRosterView and not EmployeeRosterView
    @Override
    public void save() {
        ShiftRestServiceBuilder.updateShift(shiftView.getTenantId(), shiftView,
                FailureShownRestCallback.onSuccess(sv -> {
                    withShiftView(sv);
                }));
    }
}
