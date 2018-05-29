package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.availabilityroster;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.Lockable;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.DateTimeViewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.HasGridObjects;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane.DummySublane;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl.DynamicScale;
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.roster.view.AvailabilityRosterView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

@Templated
public class AvailabilityRosterPageViewport extends DateTimeViewport<AvailabilityRosterView, AvailabilityRosterMetadata> implements IsElement {

    @Inject
    private AvailabilityRosterPageViewportBuilder viewportBuilder;
    @Inject
    private ManagedInstance<AvailabilityGridObject> employeeAvailabilityGridObjectInstances;

    private RosterState rosterState;
    private Map<Long, Spot> spotIdToSpotMap;
    private Map<Long, Employee> employeeIdToEmployeeMap;

    @PostConstruct
    private void init() {
        viewportBuilder.withViewport(this);
    }

    @Override
    protected void withView(AvailabilityRosterView view) {
        rosterState = view.getRosterState();
        spotIdToSpotMap = super.getIdMapFor(view.getSpotList(), (s) -> s.getId());
        employeeIdToEmployeeMap = super.getIdMapFor(view.getEmployeeList(), (e) -> e.getId());
    }

    @Override
    protected LinearScale<LocalDateTime> getScaleFor(AvailabilityRosterView view) {
        LocalDateTime endDateTime = LocalDateTime.of(view.getEndDate(),
                                                     LocalTime.of(0, 0, 0));
        LocalDateTime startDateTime = view.getEndDate().minusDays(20).isAfter(view.getStartDate()) ? endDateTime.minusDays(20) : LocalDateTime.of(view.getStartDate(),
                                                                                                                                                  LocalTime.of(0, 0, 0));
        return new DynamicScale(startDateTime, endDateTime, Duration.ofHours(1));
    }

    @Override
    protected Map<Long, String> getLaneTitlesFor(AvailabilityRosterView view) {
        return view.getEmployeeList().stream().collect(Collectors.toMap((s) -> s.getId(), (s) -> s.getName()));
    }

    @Override
    protected RepeatingCommand getViewportBuilderCommand(AvailabilityRosterView view, Lockable<Map<Long, Lane<LocalDateTime, AvailabilityRosterMetadata>>> lockableLaneMap) {
        return viewportBuilder.getWorkerCommand(view, lockableLaneMap, System.currentTimeMillis());
    }

    @Override
    protected Function<LocalDateTime, HasGridObjects<LocalDateTime, AvailabilityRosterMetadata>> getInstanceCreator(AvailabilityRosterView view, Long laneId) {
        final Employee employee = employeeIdToEmployeeMap.get(laneId);
        final Integer tenantId = view.getTenantId();
        return (t) -> {
            LocalDateTime startDateTime = t.minusHours(t.getHour()).minusMinutes(t.getMinute()).minusSeconds(t.getSecond());
            LocalDateTime endDateTime = startDateTime.plusDays(1);
            EmployeeAvailabilityView availability = new EmployeeAvailabilityView(tenantId, employee, startDateTime, endDateTime,
                                                                                 EmployeeAvailabilityState.UNAVAILABLE);
            AvailabilityGridObject out = employeeAvailabilityGridObjectInstances.get().withEmployeeAvailabilityView(availability);
            EmployeeRestServiceBuilder.addEmployeeAvailability(tenantId, availability, FailureShownRestCallback.onSuccess(av -> {
                out.withEmployeeAvailabilityView(av);
                getLockableLaneMap().acquire().then(laneMap -> {
                    laneMap.get(laneId).moveAddedGridObjectToIdMap(out);
                    return promiseUtils.resolve();
                });
            }));
            return out;
        };
    }

    @Override
    protected AvailabilityRosterMetadata getMetadata() {
        return new AvailabilityRosterMetadata(rosterState, spotIdToSpotMap, employeeIdToEmployeeMap);
    }

    @Override
    protected Function<LocalDateTime, String> getDateHeaderFunction() {
        DateTimeFormat dateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_FULL);
        return (date) -> dateFormat.format(GwtJavaTimeWorkaroundUtil.toDate(date));
    }

    @Override
    protected Function<LocalDateTime, String> getTimeHeaderFunction() {
        DateTimeFormat timeFormat = DateTimeFormat.getFormat(PredefinedFormat.TIME_SHORT);
        return (date) -> {
            if (date.getHour() == 0) {
                return "";
            }
            return timeFormat.format(GwtJavaTimeWorkaroundUtil.toDate(date));
        };
    }

    @Override
    protected Function<LocalDateTime, List<String>> getDateHeaderAdditionalClassesFunction() {
        return (date) -> (rosterState.isHistoric(date)) ? Collections.emptyList() : (rosterState.isPublished(date)) ? Arrays.asList("fa", "fa-check") : Arrays.asList("fa", "fa-list-alt");
    }

    @Override
    protected String getLoadingTaskId() {
        return "availability-roster";
    }

    @Override
    protected boolean showLoadingSpinner() {
        return !viewportBuilder.isSolving();
    }

    @Override
    protected DummySublane getDummySublane() {
        return DummySublane.TOP;
    }

}
