package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

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
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;

public class EmployeeDataFetchable implements Fetchable<Collection<EmployeeData>> {
    Updatable<Collection<EmployeeData>> updatable;
    Provider<Integer> tenantIdProvider;
    
    public EmployeeDataFetchable(Provider<Integer> tenantIdProvider) {
        this.tenantIdProvider = tenantIdProvider;
    }

    @Override
    public void fetchData(Command after) {
        RosterRestServiceBuilder.getCurrentEmployeeRosterView(tenantIdProvider.get(), new FailureShownRestCallback<EmployeeRosterView>() {
            @Override
            public void onSuccess(EmployeeRosterView employeeRosterView) {
                Map<Long, Map<Long, List<ShiftView>>> timeSlotIdToEmployeeIdToShiftViewListMap = employeeRosterView.
                        getTimeSlotIdToEmployeeIdToShiftViewListMap();
                
                Map<Long, Map<Long, EmployeeAvailabilityView>> timeSlotIdToEmployeeIdToAvailabilityViewMap = employeeRosterView
                        .getTimeSlotIdToEmployeeIdToAvailabilityViewMap();
                Map<Long, Spot> spotMap = employeeRosterView.getSpotList().stream()
                        .collect(Collectors.toMap(Spot::getId, Function.identity()));
                
                List<TimeSlot> timeslots = employeeRosterView.getTimeSlotList();
                List<Employee> employees = employeeRosterView.getEmployeeList();
                Collection<EmployeeData> out = new ArrayList<>();
                
                for (TimeSlot timeslot : timeslots) {
                    for (Employee employee : employees) {
                        out.add(new EmployeeData(timeslot.getStartDateTime(),timeslot.getEndDateTime(),employee,
                                timeSlotIdToEmployeeIdToAvailabilityViewMap.get(timeslot.getId()).get(employee.getId()),
                                (null != timeSlotIdToEmployeeIdToShiftViewListMap.get(timeslot.getId()).get(employee.getId()))? timeSlotIdToEmployeeIdToShiftViewListMap.get(timeslot.getId()).get(employee.getId())
                                .stream().map((e) -> spotMap.get(e.getSpotId())).collect(Collectors.toList()) : Collections.emptyList()));
                    }
                }
                updatable.onUpdate(out);
                after.execute();
            }
        });
        
    }

    @Override
    public void setUpdatable(Updatable<Collection<EmployeeData>> listener) {
        this.updatable = listener;
    }

}
