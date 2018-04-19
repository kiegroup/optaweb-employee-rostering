package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.spotroster;

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
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.DateTimeViewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.HasGridObjects;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl.DynamicScale;
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

@Templated
public class SpotRosterPageViewport extends DateTimeViewport<SpotRosterView, SpotRosterMetadata> implements IsElement {

    @Inject
    private SpotRosterPageViewportBuilder viewportBuilder;
    @Inject
    private ManagedInstance<ShiftGridObject> shiftGridObjectInstances;

    private RosterState rosterState;
    private Map<Long, Spot> spotIdToSpotMap;
    private Map<Long, Employee> employeeIdToEmployeeMap;

    @PostConstruct
    private void init() {
        viewportBuilder.withViewport(this);
    }

    @Override
    protected void withView(SpotRosterView view) {
        rosterState = view.getRosterState();
        spotIdToSpotMap = super.getIdMapFor(view.getSpotList(), (s) -> s.getId());
        employeeIdToEmployeeMap = super.getIdMapFor(view.getEmployeeList(), (s) -> s.getId());
    }

    @Override
    protected LinearScale<LocalDateTime> getScaleFor(SpotRosterView view) {
        return new DynamicScale(LocalDateTime.of(view.getStartDate(),
                                                 LocalTime.of(0, 0, 0)),
                                LocalDateTime.of(view.getEndDate(),
                                                 LocalTime.of(0, 0, 0)), Duration.ofHours(1));
    }

    @Override
    protected Map<Long, String> getLaneTitlesFor(SpotRosterView view) {
        return view.getSpotList().stream().collect(Collectors.toMap((s) -> s.getId(), (s) -> s.getName()));
    }

    @Override
    protected RepeatingCommand getViewportBuilderCommand(SpotRosterView view, Map<Long, Lane<LocalDateTime, SpotRosterMetadata>> laneMap) {
        return viewportBuilder.getWorkerCommand(view, laneMap, System.currentTimeMillis());
    }

    @Override
    protected Function<LocalDateTime, HasGridObjects<LocalDateTime, SpotRosterMetadata>> getInstanceCreator(SpotRosterView view, Long laneId) {
        final Spot spot = spotIdToSpotMap.get(laneId);
        final Integer tenantId = view.getTenantId();
        return (t) -> {
            ShiftView shift = new ShiftView(tenantId, spot, t, t.plusHours(8));
            ShiftGridObject out = shiftGridObjectInstances.get().withShiftView(shift);
            ShiftRestServiceBuilder.addShift(tenantId, shift, FailureShownRestCallback.onSuccess(sv -> {
                out.withShiftView(sv);
            }));
            return out;
        };
    }

    @Override
    protected SpotRosterMetadata getMetadata() {
        return new SpotRosterMetadata(rosterState, spotIdToSpotMap, employeeIdToEmployeeMap);
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

}
