package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.spotroster;

import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.SingleGridObject;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl.AbstractHasTimeslotGridObject;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl.Draggability;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl.Resizability;
import org.optaplanner.openshift.employeerostering.shared.common.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

@Templated
public class ShiftGridObject extends AbstractHasTimeslotGridObject<SpotRosterMetadata> implements SingleGridObject<LocalDateTime, SpotRosterMetadata> {

    @Inject
    @DataField("label")
    @Named("span")
    private HTMLElement label;

    @Inject
    private Draggability<LocalDateTime, SpotRosterMetadata> draggability;
    @Inject
    private Resizability<LocalDateTime, SpotRosterMetadata> resizability;
    @Inject
    private ManagedInstance<ShiftBlobPopoverContent> popoverInstances;

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
            label.innerHTML = (shiftView.getEmployeeId() == null) ? "Unassigned" : getLane().getMetadata()
                                                                                            .getEmployeeIdToEmployeeMap().get(shiftView.getEmployeeId()).getName();
            setClassProperty("pinned", shiftView.isPinnedByUser());
            setClassProperty("unassigned", shiftView.getEmployeeId() == null);
            RosterState rosterState = getLane().getMetadata().getRosterState();
            setClassProperty("historic", rosterState.isHistoric(shiftView));
            setClassProperty("published", rosterState.isPublished(shiftView));
            setClassProperty("draft", rosterState.isDraft(shiftView));
        }
    }

    @Override
    protected void onMouseClick(MouseEvent e) {
        if (e.shiftKey) {
            getLane().removeGridObject(this);
        } else {
            popoverInstances.get().init(this);
        }
    }

    @Override
    protected HasTimeslot getTimeslot() {
        return shiftView;
    }

    @Override
    protected void init(Lane<LocalDateTime, SpotRosterMetadata> lane) {
        draggability.applyFor(this, lane.getScale());
        resizability.applyFor(this, lane.getScale());
        refresh();
    }

    public Spot getSpot() {
        return getLane().getMetadata().getSpotIdToSpotMap().get(shiftView.getSpotId());
    }

    public Employee getEmployee() {
        return getLane().getMetadata().getEmployeeIdToEmployeeMap().get(shiftView.getEmployeeId());
    }

    @Override
    public void save() {
        ShiftRestServiceBuilder.updateShift(shiftView.getTenantId(), shiftView,
                                            FailureShownRestCallback.onSuccess(sv -> {
                                                withShiftView(sv);
                                            }));
    }
}
