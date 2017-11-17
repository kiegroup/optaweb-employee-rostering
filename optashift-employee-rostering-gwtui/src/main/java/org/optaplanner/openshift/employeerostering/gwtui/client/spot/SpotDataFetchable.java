package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Provider;

import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Fetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;

public class SpotDataFetchable implements Fetchable<Collection<SpotData>> {
    Updatable<Collection<SpotData>> updatable;
    Provider<Integer> tenantIdProvider;
    
    public SpotDataFetchable(Provider<Integer> tenantIdProvider) {
        this.tenantIdProvider = tenantIdProvider;
    }

    @Override
    public void fetchData(Command after) {
        RosterRestServiceBuilder.getCurrentSpotRosterView(tenantIdProvider.get(), new FailureShownRestCallback<SpotRosterView>() {
            @Override
            public void onSuccess(SpotRosterView spotRosterView) {
                Map<Long, Map<Long, List<ShiftView>>> timeSlotIdToSpotIdToShiftViewListMap = spotRosterView
                        .getTimeSlotIdToSpotIdToShiftViewListMap();
                Map<Long, Employee> employeeMap = spotRosterView.getEmployeeList().stream()
                        .collect(Collectors.toMap(Employee::getId, Function.identity()));
                
                List<TimeSlot> timeslots = spotRosterView.getTimeSlotList();
                List<Spot> spots = spotRosterView.getSpotList();
                Collection<SpotData> out = new ArrayList<>();
                
                for (TimeSlot timeslot : timeslots) {
                    for (Spot spot : spots) {
                        out.add(new SpotData(timeslot.getStartDateTime(), timeslot.getEndDateTime(),spot,
                                (null != timeSlotIdToSpotIdToShiftViewListMap.get(timeslot.getId()).get(spot.getId()))? timeSlotIdToSpotIdToShiftViewListMap.get(timeslot.getId()).get(spot.getId())
                                        .stream().map((e) -> employeeMap.get(e.getEmployeeId())).collect(Collectors.toList()) : Collections.emptyList()));
                    }
                }
                updatable.onUpdate(out);
                after.execute();
            }
        });
        
    }

    @Override
    public void setUpdatable(Updatable<Collection<SpotData>> listener) {
        this.updatable = listener;
    }

}
