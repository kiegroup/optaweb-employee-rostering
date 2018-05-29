package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.rotation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.Lockable;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.DateTimeViewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.HasGridObjects;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane.DummySublane;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl.DynamicScale;
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;
import org.optaplanner.openshift.employeerostering.shared.common.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.RotationView;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.ROTATION_SAVE;

@Templated
public class RotationPageViewport extends DateTimeViewport<RotationView, RotationMetadata> implements IsElement {

    @Inject
    private RotationPageViewportBuilder viewportBuilder;
    @Inject
    private ManagedInstance<ShiftTemplateModel> shiftTemplateModelInstances;
    @Inject
    private TenantStore tenantStore;
    @Inject
    private EventManager eventManager;
    @Inject
    private PromiseUtils promiseUtils;

    private Map<Long, Spot> spotIdToSpotMap;
    private Map<Long, Employee> employeeIdToEmployeeMap;

    public static final LocalDateTime BASE_DATE = HasTimeslot.EPOCH;

    @PostConstruct
    private void init() {
        viewportBuilder.withViewport(this);
        eventManager.subscribeToEvent(ROTATION_SAVE, (v) -> saveRotation());
    }

    @Override
    protected void withView(RotationView view) {
        spotIdToSpotMap = super.getIdMapFor(view.getSpotList(), (s) -> s.getId());
        employeeIdToEmployeeMap = super.getIdMapFor(view.getEmployeeList(), (s) -> s.getId());
    }

    @Override
    protected LinearScale<LocalDateTime> getScaleFor(RotationView view) {
        return new DynamicScale(BASE_DATE,
                                BASE_DATE.plusDays(view.getRotationLength()),
                                Duration.ofHours(1));
    }

    @Override
    protected Map<Long, String> getLaneTitlesFor(RotationView view) {
        return view.getSpotList().stream().collect(Collectors.toMap((s) -> s.getId(), (s) -> s.getName()));
    }

    @Override
    protected RepeatingCommand getViewportBuilderCommand(RotationView view, Lockable<Map<Long, Lane<LocalDateTime, RotationMetadata>>> lockableLaneMap) {
        return viewportBuilder.getWorkerCommand(view, lockableLaneMap, System.currentTimeMillis());
    }

    @Override
    protected Function<LocalDateTime, HasGridObjects<LocalDateTime, RotationMetadata>> getInstanceCreator(RotationView view, Long laneId) {
        final Spot spot = spotIdToSpotMap.get(laneId);
        return (t) -> {
            ShiftTemplateView newInstance = new ShiftTemplateView();
            newInstance.setSpotId(spot.getId());
            newInstance.setTenantId(tenantStore.getCurrentTenantId());
            newInstance.setDurationBetweenRotationStartAndTemplateStart(
                                                                        Duration.between(BASE_DATE, t));
            newInstance.setShiftTemplateDuration(Duration.ofHours(8));
            return shiftTemplateModelInstances.get().withShiftTemplateView(newInstance);
        };
    }

    @Override
    protected RotationMetadata getMetadata() {
        return new RotationMetadata(spotIdToSpotMap, employeeIdToEmployeeMap);
    }

    @Override
    protected Function<LocalDateTime, String> getDateHeaderFunction() {
        // TODO: i18n
        return (date) -> "Day " + (Duration.between(BASE_DATE, date).toDays() + 1);
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
        return (date) -> Collections.emptyList();
    }

    private void saveRotation() {
        getLockableLaneMap().acquire().then(laneMap -> {
            Map<Long, List<ShiftTemplateView>> newSpotIdToShiftTemplateViewListMap = new HashMap<>();

            for (Long spotId : laneMap.keySet()) {
                Lane<LocalDateTime, RotationMetadata> lane = laneMap.get(spotId);
                Collection<ShiftTemplateModel> shiftTemplateMap = lane.getGridObjects(ShiftTemplateModel.class);
                List<ShiftTemplateView> shiftTemplateList = new ArrayList<>();
                shiftTemplateMap.forEach((template) -> shiftTemplateList.add(template.getShiftTemplateView()));
                newSpotIdToShiftTemplateViewListMap.put(spotId, shiftTemplateList);
            }
            RotationView rotationView = new RotationView();
            rotationView.setTenantId(tenantStore.getCurrentTenantId());
            rotationView.setSpotIdToShiftTemplateViewListMap(newSpotIdToShiftTemplateViewListMap);
            ShiftRestServiceBuilder.updateRotation(tenantStore.getCurrentTenantId(), rotationView,
                                                   FailureShownRestCallback.onSuccess(e -> {
                                                       viewportBuilder.buildRotationViewport(this);
                                                   }));
            return promiseUtils.resolve();
        });
    }

    @Override
    protected DummySublane getDummySublane() {
        return DummySublane.BOTTOM;
    }

    @Override
    protected String getLoadingTaskId() {
        return "rotation";
    }

    @Override
    protected boolean showLoadingSpinner() {
        return true;
    }

}
