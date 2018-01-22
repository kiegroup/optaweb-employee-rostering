package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Provider;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Calendar;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeData;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Fetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.LoadingPopup;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlotUtils;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;

public class SpotDataFetchable implements Fetchable<Collection<SpotData>> {

    Updatable<Collection<SpotData>> updatable;
    Provider<Integer> tenantIdProvider;
    SpotRosterView last;
    Calendar<SpotId, SpotData> calendar;
    static final String LOADING_STRING = "Fetching Spot Roster";
    boolean busy;

    public SpotDataFetchable(Provider<Integer> tenantIdProvider) {
        this(null, tenantIdProvider);
    }

    public SpotDataFetchable(Calendar<SpotId, SpotData> calendar, Provider<Integer> tenantIdProvider) {
        this.calendar = calendar;
        this.tenantIdProvider = tenantIdProvider;
        last = null;
        busy = false;
    }

    @Override
    public void fetchData(Command after) {
        if (!busy) {
            busy = true;
            Integer tenantId = tenantIdProvider.get();
            if (null == last || null == calendar || !last.getTenantId().equals(tenantId)) {
                LoadingPopup.setLoading(LOADING_STRING);
                RosterRestServiceBuilder.getCurrentSpotRosterView(tenantId, new FailureShownRestCallback<
                        SpotRosterView>() {

                    @Override
                    public void onSuccess(SpotRosterView spotRosterView) {
                        try {
                            last = spotRosterView;
                            Map<Long, Map<Long, List<ShiftView>>> timeSlotIdToSpotIdToShiftViewListMap = spotRosterView
                                    .getTimeSlotIdToSpotIdToShiftViewListMap();
                            Map<Long, Employee> employeeMap = spotRosterView.getEmployeeList().stream()
                                    .collect(Collectors.toMap(Employee::getId, Function.identity()));

                            List<TimeSlot> timeslots = spotRosterView.getTimeSlotList();
                            List<Spot> spots = spotRosterView.getSpotList();
                            Collection<SpotData> out = new ArrayList<>();

                            for (TimeSlot timeslot : timeslots) {
                                for (Spot spot : spots) {
                                    if (null != timeSlotIdToSpotIdToShiftViewListMap.getOrDefault(timeslot.getId(),
                                            Collections.emptyMap()).get(spot
                                                    .getId())) {
                                        timeSlotIdToSpotIdToShiftViewListMap.get(timeslot.getId()).get(spot.getId())
                                                .stream().forEach((sv) -> {
                                                    Shift shift = new Shift(sv, spot, timeslot);
                                                    shift.setEmployee(employeeMap.get(sv.getEmployeeId()));
                                                    out.add(new SpotData(shift));
                                                });
                                    }
                                }
                            }
                            updatable.onUpdate(out);
                            after.execute();
                            calendar.setHardStartDateBound(spotRosterView.getStartDate().atTime(0, 0));
                            calendar.setHardEndDateBound(spotRosterView.getEndDate().atTime(0, 0));
                        } finally {
                            LoadingPopup.clearLoading(LOADING_STRING);
                        }
                    }
                });
            } else {
                RosterRestServiceBuilder.getSpotRosterViewFor(tenantId, calendar.getViewStartDate().minusDays(1)
                        .toLocalDate()
                        .toString(),
                        calendar.getViewEndDate().plusDays(1).toLocalDate().toString(), calendar.getVisibleGroupSet()
                                .stream()
                                .map((g) -> g.getSpot()).collect(Collectors.toList()), new FailureShownRestCallback<
                                        SpotRosterView>() {

                                    @Override
                                    public void onSuccess(SpotRosterView spotRosterView) {
                                        last = spotRosterView;
                                        Map<Long, Map<Long, List<ShiftView>>> timeSlotIdToSpotIdToShiftViewListMap =
                                                spotRosterView
                                                        .getTimeSlotIdToSpotIdToShiftViewListMap();
                                        Map<Long, Employee> employeeMap = spotRosterView.getEmployeeList().stream()
                                                .collect(Collectors.toMap(Employee::getId, Function.identity()));

                                        List<TimeSlot> timeslots = spotRosterView.getTimeSlotList();
                                        List<Spot> spots = spotRosterView.getSpotList();

                                        for (TimeSlot timeslot : timeslots) {
                                            for (Spot spot : spots) {
                                                if (null != timeSlotIdToSpotIdToShiftViewListMap.getOrDefault(timeslot
                                                        .getId(), Collections.emptyMap()).get(
                                                                spot.getId())) {
                                                    timeSlotIdToSpotIdToShiftViewListMap.get(timeslot.getId()).get(spot
                                                            .getId())
                                                            .stream().forEach((sv) -> {
                                                                Shift shift = new Shift(sv, spot, timeslot);
                                                                shift.setEmployee(employeeMap.get(sv.getEmployeeId()));
                                                                SpotData newShift = new SpotData(shift);
                                                                calendar.updateShift(newShift);
                                                            });
                                                }
                                            }
                                        }
                                        after.execute();
                                    }
                                });

            }
            busy = false;
        }

    }

    @Override
    public void setUpdatable(Updatable<Collection<SpotData>> listener) {
        this.updatable = listener;
    }

}
