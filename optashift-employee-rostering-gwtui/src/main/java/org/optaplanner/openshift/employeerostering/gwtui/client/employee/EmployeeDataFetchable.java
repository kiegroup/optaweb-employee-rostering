package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Provider;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Calendar;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Fetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotData;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;

public class EmployeeDataFetchable implements Fetchable<Collection<EmployeeData>> {

    Updatable<Collection<EmployeeData>> updatable;
    Provider<Integer> tenantIdProvider;
    EmployeeRosterView last;
    Calendar<EmployeeId, EmployeeData> calendar;

    public EmployeeDataFetchable(Calendar<EmployeeId, EmployeeData> calendar, Provider<Integer> tenantIdProvider) {
        this.calendar = calendar;
        this.tenantIdProvider = tenantIdProvider;
        last = null;
    }

    @Override
    public void fetchData(Command after) {
        Integer tenantId = tenantIdProvider.get();
        if (null == last || !last.getTenantId().equals(tenantId)) {
            RosterRestServiceBuilder.getCurrentEmployeeRosterView(tenantId, new FailureShownRestCallback<
                    EmployeeRosterView>() {

                @Override
                public void onSuccess(EmployeeRosterView employeeRosterView) {
                    last = employeeRosterView;
                    Map<Long, Map<Long, List<ShiftView>>> timeSlotIdToEmployeeIdToShiftViewListMap = employeeRosterView
                            .getTimeSlotIdToEmployeeIdToShiftViewListMap();

                    Map<Long, Map<Long, EmployeeAvailabilityView>> timeSlotIdToEmployeeIdToAvailabilityViewMap =
                            employeeRosterView
                                    .getTimeSlotIdToEmployeeIdToAvailabilityViewMap();
                    Map<Long, Spot> spotMap = employeeRosterView.getSpotList().stream()
                            .collect(Collectors.toMap(Spot::getId, Function.identity()));

                    List<TimeSlot> timeslots = employeeRosterView.getTimeSlotList();
                    List<Employee> employees = employeeRosterView.getEmployeeList();
                    Collection<EmployeeData> out = new ArrayList<>();

                    EmployeeData.resetData();
                    for (TimeSlot timeslot : timeslots) {
                        for (Employee employee : employees) {
                            if (null != timeSlotIdToEmployeeIdToShiftViewListMap.get(timeslot.getId()).get(employee
                                    .getId())) {
                                timeSlotIdToEmployeeIdToShiftViewListMap.get(timeslot.getId()).get(employee.getId())
                                        .stream().forEach((sv) -> {
                                            Shift shift = new Shift(sv, null, timeslot);
                                            shift.setEmployee(employee);
                                            shift.setSpot(spotMap.get(sv.getSpotId()));
                                            out.add(new EmployeeData(shift,
                                                    timeSlotIdToEmployeeIdToAvailabilityViewMap.get(timeslot.getId())
                                                            .get(
                                                                    employee.getId())));
                                        });
                            } else {
                                Shift shift = new Shift();
                                shift.setTenantId(employee.getTenantId());
                                shift.setEmployee(employee);
                                shift.setTimeSlot(timeslot);
                                out.add(new EmployeeData(shift,
                                        timeSlotIdToEmployeeIdToAvailabilityViewMap.get(timeslot.getId()).get(employee
                                                .getId())));
                            }
                        }
                    }
                    updatable.onUpdate(out);
                    after.execute();
                }
            });
        } else {
            RosterRestServiceBuilder.getEmployeeRosterViewFor(tenantId, calendar.getViewStartDate().toLocalDate()
                    .toString(),
                    calendar.getViewEndDate().toLocalDate().toString(), calendar.getVisibleGroups().stream()
                            .map((g) -> g.getEmployee()).collect(Collectors.toList()),
                    new FailureShownRestCallback<
                            EmployeeRosterView>() {

                        @Override
                        public void onSuccess(EmployeeRosterView employeeRosterView) {
                            last = employeeRosterView;
                            Map<Long, Map<Long, List<ShiftView>>> timeSlotIdToEmployeeIdToShiftViewListMap =
                                    employeeRosterView
                                            .getTimeSlotIdToEmployeeIdToShiftViewListMap();

                            Map<Long, Map<Long, EmployeeAvailabilityView>> timeSlotIdToEmployeeIdToAvailabilityViewMap =
                                    employeeRosterView
                                            .getTimeSlotIdToEmployeeIdToAvailabilityViewMap();
                            Map<Long, Spot> spotMap = employeeRosterView.getSpotList().stream()
                                    .collect(Collectors.toMap(Spot::getId, Function.identity()));

                            List<TimeSlot> timeslots = employeeRosterView.getTimeSlotList();
                            List<Employee> employees = employeeRosterView.getEmployeeList();

                            for (TimeSlot timeslot : timeslots) {
                                for (Employee employee : employees) {
                                    if (null != timeSlotIdToEmployeeIdToShiftViewListMap.get(timeslot.getId()).get(
                                            employee
                                                    .getId())) {
                                        timeSlotIdToEmployeeIdToShiftViewListMap.get(timeslot.getId()).get(employee
                                                .getId())
                                                .stream().forEach((sv) -> {
                                                    Shift shift = new Shift(sv, null, timeslot);
                                                    shift.setEmployee(employee);
                                                    shift.setSpot(spotMap.get(sv.getSpotId()));
                                                    EmployeeData.update(shift,
                                                            timeSlotIdToEmployeeIdToAvailabilityViewMap.get(timeslot
                                                                    .getId())
                                                                    .get(
                                                                            employee.getId()));
                                                });
                                    } else {
                                        Shift shift = new Shift();
                                        shift.setTenantId(employee.getTenantId());
                                        shift.setEmployee(employee);
                                        shift.setTimeSlot(timeslot);
                                        EmployeeData.update(shift,
                                                timeSlotIdToEmployeeIdToAvailabilityViewMap.get(timeslot.getId()).get(
                                                        employee
                                                                .getId()));
                                    }
                                }
                            }
                            after.execute();
                        }
                    });
        }
    }

    @Override
    public void setUpdatable(Updatable<Collection<EmployeeData>> listener) {
        this.updatable = listener;
    }

}
