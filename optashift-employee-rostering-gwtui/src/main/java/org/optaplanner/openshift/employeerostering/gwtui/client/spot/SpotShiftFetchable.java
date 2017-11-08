package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Provider;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.ShiftData;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Fetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;

public class SpotShiftFetchable implements Fetchable<Collection<ShiftData>> {
    Updatable<Collection<ShiftData>> updatable;
    Provider<Integer> tenantIdProvider;
    
    public SpotShiftFetchable(Provider<Integer> tenantIdProvider) {
        this.tenantIdProvider = tenantIdProvider;
    }
    
    @Override
    public void fetchData(Command after) {
        ShiftRestServiceBuilder.getShifts(tenantIdProvider.get(), new FailureShownRestCallback<List<Shift>>() {
            @Override
            public void onSuccess(List<Shift> theShifts) {
                List<ShiftData> shifts = new ArrayList<>();
                LocalDateTime min = theShifts.stream()
                        .min((a,b) -> a.getTimeSlot().getStartDateTime().compareTo(b.getTimeSlot().getStartDateTime()))
                        .get().getTimeSlot().getStartDateTime();
                for (Shift shift : theShifts) {
                    shifts.add(new ShiftData(shift.getTimeSlot().getStartDateTime().minusSeconds(min.toEpochSecond(ZoneOffset.UTC)),
                            shift.getTimeSlot().getEndDateTime().minusSeconds(min.toEpochSecond(ZoneOffset.UTC)),
                            shift.getSpot().getName()));
                }
                updatable.onUpdate(shifts);
                after.execute();
            }
        });
    }

    @Override
    public void setUpdatable(Updatable<Collection<ShiftData>> listener) {
        updatable = listener;
    }

}
