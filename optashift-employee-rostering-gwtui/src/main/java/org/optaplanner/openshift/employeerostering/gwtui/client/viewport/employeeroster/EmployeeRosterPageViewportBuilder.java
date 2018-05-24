package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.employeeroster;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import elemental2.promise.Promise;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.Lockable;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.EMPLOYEE_ROSTER_PAGINATION;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.EMPLOYEE_ROSTER_UPDATE;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_END;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_START;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.EMPLOYEE_ROSTER_INVALIDATE;

@Singleton
public class EmployeeRosterPageViewportBuilder {

    @Inject
    private PromiseUtils promiseUtils;

    @Inject
    private CommonUtils commonUtils;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private ManagedInstance<ShiftGridObject> shiftGridObjectInstances;

    @Inject
    private ManagedInstance<EmployeeAvailabilityGridObject> employeeAvailabilityGridObjectInstances;

    @Inject
    private EventManager eventManager;

    private EmployeeRosterPageViewport viewport;

    private boolean isUpdatingRoster;
    private boolean isSolving;

    private final int WORK_LIMIT_PER_CYCLE = 50;

    private long currentWorkerStartTime;
    private Pagination pagination;

    @PostConstruct
    private void init() {
        pagination = Pagination.of(0, 10);
        eventManager.subscribeToEvent(SOLVE_START, (m) -> this.onSolveStart());
        eventManager.subscribeToEvent(SOLVE_END, (m) -> this.onSolveEnd());
        eventManager.subscribeToEvent(EMPLOYEE_ROSTER_PAGINATION, (pagination) -> {
            this.pagination = pagination;
            buildEmployeeRosterViewport(viewport);
        });
        eventManager.subscribeToEvent(EMPLOYEE_ROSTER_INVALIDATE, (nil) -> {
            buildEmployeeRosterViewport(viewport);
        });
    }

    public EmployeeRosterPageViewportBuilder withViewport(EmployeeRosterPageViewport viewport) {
        this.viewport = viewport;
        return this;
    }

    public RepeatingCommand getWorkerCommand(final EmployeeRosterView view, final Lockable<Map<Long, Lane<LocalDateTime, EmployeeRosterMetadata>>> lockableLaneMap, final long timeWhenInvoked) {
        if (view.getEmployeeList().isEmpty()) {
            eventManager.fireEvent(EMPLOYEE_ROSTER_PAGINATION, pagination.previousPage());
            return () -> false;
        }

        currentWorkerStartTime = timeWhenInvoked;

        final Iterator<ShiftView> shiftViewsToAdd = commonUtils.flatten(view.getEmployeeIdToShiftViewListMap().values()).iterator();
        final Iterator<EmployeeAvailabilityView> employeeAvaliabilitiesViewsToAdd = commonUtils.flatten(view.getEmployeeIdToAvailabilityViewListMap().values()).iterator();

        setUpdatingRoster(true);
        eventManager.fireEvent(EMPLOYEE_ROSTER_UPDATE, view);

        return new RepeatingCommand() {

            final long timeWhenStarted = timeWhenInvoked;

            @Override
            public boolean execute() {
                if (timeWhenStarted != getCurrentWorkerStartTime()) {
                    return false;
                }
                lockableLaneMap.acquireIfPossible(laneMap -> {
                    int workDone = 0;
                    while (shiftViewsToAdd.hasNext() && workDone < WORK_LIMIT_PER_CYCLE) {
                        ShiftView toAdd = shiftViewsToAdd.next();
                        laneMap.get(toAdd.getEmployeeId()).addOrUpdateGridObject(
                                ShiftGridObject.class, toAdd.getId(), () -> {
                                    ShiftGridObject out = shiftGridObjectInstances.get();
                                    out.withShiftView(toAdd);
                                    return out;
                                }, (s) -> {
                                    s.withShiftView(toAdd);
                                    return null;
                                });
                        workDone++;
                    }
    
                    while (employeeAvaliabilitiesViewsToAdd.hasNext() && workDone < WORK_LIMIT_PER_CYCLE) {
                        EmployeeAvailabilityView toAdd = employeeAvaliabilitiesViewsToAdd.next();
                        laneMap.get(toAdd.getEmployeeId()).addOrUpdateGridObject(
                                EmployeeAvailabilityGridObject.class, toAdd.getId(), () -> {
                                    EmployeeAvailabilityGridObject out = employeeAvailabilityGridObjectInstances.get();
                                    out.withEmployeeAvailabilityView(toAdd);
                                    return out;
                                }, (a) -> {
                                    a.withEmployeeAvailabilityView(toAdd);
                                    return null;
                                });
                        workDone++;
                    }
    
                    if (!shiftViewsToAdd.hasNext() && !employeeAvaliabilitiesViewsToAdd.hasNext()) {
                        laneMap.forEach((l, lane) -> lane.endModifying());
                        setUpdatingRoster(false);
                    }
                });
                return shiftViewsToAdd.hasNext();
            }
        };
    }

    private void setUpdatingRoster(boolean isUpdatingRoster) {
        this.isUpdatingRoster = isUpdatingRoster;
    }

    private boolean isSolving() {
        return isSolving;
    }

    private long getCurrentWorkerStartTime() {
        return currentWorkerStartTime;
    }

    public void onSolveStart() {
        isSolving = true;
        Scheduler.get().scheduleFixedPeriod(() -> {
            if (!isUpdatingRoster) {
                setUpdatingRoster(true);
                getEmployeeRosterView().then(srv -> {
                    viewport.refresh(srv);
                    return promiseUtils.resolve();
                });
            }
            return isSolving();
        }, 2000);
    }

    public void onSolveEnd() {
        isSolving = false;
    }

    public Promise<Void> buildEmployeeRosterViewport(final EmployeeRosterPageViewport toBuild) {
        return getEmployeeRosterView().then((erv) -> {
            toBuild.refresh(erv);
            return promiseUtils.resolve();
        });
    }

    public Promise<EmployeeRosterView> getEmployeeRosterView() {
        return promiseUtils.promise((res, rej) -> {
            RosterRestServiceBuilder.getCurrentEmployeeRosterView(tenantStore.getCurrentTenantId(), pagination.getPageNumber(), pagination.getNumberOfItemsPerPage(),
                    FailureShownRestCallback.onSuccess((s) -> {
                        res.onInvoke(s);
                    }));
        });
    }
}
