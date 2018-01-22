package org.optaplanner.openshift.employeerostering.gwtui.client.tenant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.ShiftData;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Fetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotData;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.IdOrGroup;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftInfo;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotGroup;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

public class TenantTemplateFetchable implements Fetchable<Collection<ShiftData>> {

    private Supplier<Integer> tenantIdSupplier;
    private Updatable<Collection<ShiftData>> updatable;
    Integer tenantId;
    Long id;

    public TenantTemplateFetchable(Supplier<Integer> tenantIdSupplier) {
        this.tenantIdSupplier = tenantIdSupplier;
        tenantId = null;
    }

    @Override
    public void fetchData(Command after) {
        final Integer TENANT_ID = tenantIdSupplier.get();
        if (tenantId != TENANT_ID) {
            tenantId = TENANT_ID;
            SpotRestServiceBuilder.getSpotList(tenantId, new FailureShownRestCallback<List<Spot>>() {

                @Override
                public void onSuccess(List<Spot> spotList) {
                    SpotRestServiceBuilder.getSpotGroups(tenantId, new FailureShownRestCallback<List<SpotGroup>>() {

                        @Override
                        public void onSuccess(List<SpotGroup> spotGroupList) {
                            ShiftRestServiceBuilder.getTemplate(tenantIdSupplier.get(),
                                    new FailureShownRestCallback<
                                            ShiftTemplate>() {

                                        @Override
                                        public void onSuccess(ShiftTemplate template) {
                                            id = 0L;
                                            List<ShiftData> out = new ArrayList<>();
                                            for (ShiftInfo shift : template.getShiftList()) {
                                                for (IdOrGroup spotId : shift.getSpotList()) {
                                                    if (spotId.getIsGroup()) {
                                                        for (Spot spot : spotGroupList.stream().filter((g) -> g.getId()
                                                                .equals(
                                                                        spotId.getItemId())).findAny().get()
                                                                .getSpots()) {
                                                            Shift newShift = new Shift(TENANT_ID,
                                                                    spot, new TimeSlot(tenantId, shift.getStartTime(),
                                                                            shift
                                                                                    .getEndTime()));
                                                            newShift.setId(id);
                                                            id++;
                                                            out.add(new ShiftData(new SpotData(newShift)));
                                                        }
                                                    } else {
                                                        Spot spot = spotList.stream().filter((s) -> s.getId().equals(
                                                                spotId.getItemId())).findAny().get();
                                                        Shift newShift = new Shift(TENANT_ID,
                                                                spot, new TimeSlot(tenantId, shift.getStartTime(), shift
                                                                        .getEndTime()));
                                                        newShift.setId(id);
                                                        id++;
                                                        out.add(new ShiftData(new SpotData(newShift)));
                                                    }

                                                }
                                            }
                                            updatable.onUpdate(out);
                                            after.execute();
                                        }
                                    });

                        }

                    });

                }
            });
        } else {
            after.execute();
        }
    }

    @Override
    public void setUpdatable(Updatable<Collection<ShiftData>> listener) {
        updatable = listener;
    }

    public Long getFreshId() {
        Long out = id;
        id++;
        return out;
    }

}
