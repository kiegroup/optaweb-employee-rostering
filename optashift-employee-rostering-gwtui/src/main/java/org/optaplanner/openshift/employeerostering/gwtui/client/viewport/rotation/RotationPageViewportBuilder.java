package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.rotation;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import elemental2.promise.Promise;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.RotationView;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;

@Singleton
public class RotationPageViewportBuilder {

    @Inject
    private PromiseUtils promiseUtils;

    @Inject
    private CommonUtils commonUtils;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private ManagedInstance<ShiftTemplateModel> shiftTemplateModelInstances;

    private RotationPageViewport viewport;

    private boolean isUpdatingRoster;
    private boolean isSolving;

    private final int WORK_LIMIT_PER_CYCLE = 50;

    private long currentWorkerStartTime;

    @PostConstruct
    private void init() {
        ErraiBus.get().subscribe("SolveStart", (m) -> this.onSolveStart());
        ErraiBus.get().subscribe("SolveEnd", (m) -> this.onSolveEnd());
    }

    public RotationPageViewportBuilder withViewport(RotationPageViewport viewport) {
        this.viewport = viewport;
        return this;
    }

    public RepeatingCommand getWorkerCommand(final RotationView view, final Map<Long, Lane<LocalDateTime, RotationMetadata>> laneMap, final long timeWhenInvoked) {
        currentWorkerStartTime = timeWhenInvoked;
        final Iterator<ShiftTemplateView> shiftTemplateViewsToAdd = commonUtils.flatten(view.getSpotIdToShiftTemplateViewListMap().values()).iterator();
        setUpdatingRoster(true);

        return new RepeatingCommand() {

            final long timeWhenStarted = timeWhenInvoked;

            @Override
            public boolean execute() {
                if (timeWhenStarted != getCurrentWorkerStartTime()) {
                    return false;
                }
                int workDone = 0;
                while (shiftTemplateViewsToAdd.hasNext() && workDone < WORK_LIMIT_PER_CYCLE) {
                    ShiftTemplateView toAdd = shiftTemplateViewsToAdd.next();
                    laneMap.get(toAdd.getSpotId()).addOrUpdateGridObject(
                            ShiftTemplateModel.class, toAdd.getId(), () -> {
                                ShiftTemplateModel out = shiftTemplateModelInstances.get();
                                out.withShiftTemplateView(toAdd);
                                return out;
                            }, (s) -> {
                                s.withShiftTemplateView(toAdd);
                                return null;
                            });
                    workDone++;
                }

                if (!shiftTemplateViewsToAdd.hasNext()) {
                    laneMap.forEach((l, lane) -> lane.endModifying());
                    setUpdatingRoster(false);
                }
                return shiftTemplateViewsToAdd.hasNext();
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
                getRotationView().then(srv -> {
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

    public Promise<Void> buildRotationViewport(final RotationPageViewport toBuild) {
        return getRotationView().then((srv) -> {
            toBuild.refresh(srv);
            return promiseUtils.resolve();
        });
    }

    public Promise<RotationView> getRotationView() {
        return promiseUtils.promise((res, rej) -> {
            ShiftRestServiceBuilder.getRotation(tenantStore.getCurrentTenantId(),
                    FailureShownRestCallback.onSuccess((rv) -> {
                        res.onInvoke(rv);
                    }));
        });
    }
}
