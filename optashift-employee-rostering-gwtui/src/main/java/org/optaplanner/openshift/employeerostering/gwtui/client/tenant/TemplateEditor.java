package org.optaplanner.openshift.employeerostering.gwtui.client.tenant;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import elemental2.dom.HTMLCanvasElement;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Calendar;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.DateDisplay;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.ShiftData;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.ShiftDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.ConstantFetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotData;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotId;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotNameFetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotShiftFetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.ConfigurationEditor.Views;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeGroup;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.EmployeeTimeSlotInfo;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.IdOrGroup;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftInfo;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotGroup;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.*;

@Templated
public class TemplateEditor implements IsElement {

    private Integer tenantId = null;

    @Inject
    @DataField
    private Button backButton;

    @Inject
    @DataField
    private Button saveButton;

    @Inject
    @DataField
    private Div container;

    @Inject
    private TranslationService CONSTANTS;

    @Inject
    private SyncBeanManager beanManager;

    private Calendar<SpotId, ShiftData> calendar;

    private ConfigurationEditor configurationEditor;

    private TenantTemplateFetchable templateFetchable;

    public TemplateEditor() {
    }

    @PostConstruct
    protected void initWidget() {
        templateFetchable = new TenantTemplateFetchable(() -> getTenantId());
        calendar = new Calendar.Builder<SpotId, ShiftData, ShiftDrawable>(container, tenantId, CONSTANTS)
                .fetchingDataFrom(templateFetchable)
                .fetchingGroupsFrom(new SpotNameFetchable(() -> getTenantId()))
                .displayWeekAs(DateDisplay.WEEKS_FROM_EPOCH)
                .withBeanManager(beanManager)
                .creatingDataInstancesWith((c, name, start, end) -> {
                    Shift newShift = new Shift(tenantId,
                            name.getSpot(),
                            new TimeSlot(tenantId,
                                    start,
                                    end));
                    newShift.setId(templateFetchable.getFreshId());
                    c
                            .addShift(new ShiftData(new SpotData(newShift)));
                })
                .asTwoDayView((v, d, i) -> new ShiftDrawable(v, d, i));
        calendar.setHardStartDateBound(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
        Window.addResizeHandler((e) -> calendar.setViewSize(e.getWidth() - container.getAbsoluteLeft(),
                e.getHeight() - container.getAbsoluteTop() - saveButton.getOffsetHeight()));
    }

    public void onAnyTenantEvent(@Observes Tenant tenant) {
        tenantId = tenant.getId();
        calendar.setTenantId(tenantId);
        calendar.setHardEndDateBound(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC).plusWeeks(tenant
                .getConfiguration().getTemplateDuration()));
        //TODO: Also handle week start date
        refresh();
    }

    private Integer getTenantId() {
        return tenantId;
    }

    public void refresh() {
        calendar.setViewSize(Window.getClientWidth() - container.getAbsoluteLeft(),
                Window.getClientHeight() - container.getAbsoluteTop() - saveButton.getOffsetHeight());
        calendar.refresh();
    }

    public void setConfigurationEditor(ConfigurationEditor configurationEditor) {
        this.configurationEditor = configurationEditor;
    }

    @EventHandler("backButton")
    private void onBackButtonClick(ClickEvent e) {
        configurationEditor.switchView(Views.TENANT_CONFIGURATION_EDITOR);
    }

    @EventHandler("saveButton")
    private void onSaveButtonClick(ClickEvent e) {
        Collection<ShiftData> shifts = calendar.getShiftSet();
        EmployeeRestServiceBuilder.findEmployeeGroupByName(tenantId, "ALL", new FailureShownRestCallback<
                EmployeeGroup>() {

            @Override
            public void onSuccess(EmployeeGroup allEmployees) {
                shifts.forEach((s) -> s.setEmployeeList(Arrays.asList(new EmployeeTimeSlotInfo(tenantId, new IdOrGroup(
                        tenantId, true,
                        allEmployees.getId()), EmployeeAvailabilityState.DESIRED))));
                List<ShiftInfo> shiftInfos = new ArrayList<>();
                shifts.forEach((s) -> shiftInfos.add(new ShiftInfo(tenantId, s)));

                ShiftRestServiceBuilder.createTemplate(tenantId, shiftInfos, new FailureShownRestCallback<
                        Void>() {

                    @Override
                    public void onSuccess(Void result) {
                    }
                });
            }
        });
    }

}
