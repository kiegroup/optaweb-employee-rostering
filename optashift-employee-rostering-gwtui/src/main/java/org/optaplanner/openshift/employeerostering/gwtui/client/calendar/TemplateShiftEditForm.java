package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.FormPopup;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;

@Templated
public class TemplateShiftEditForm implements IsElement {

    @Inject
    @DataField
    private CheckBox hasPreferredEmployee;

    @Inject
    @DataField
    private ListBox preferredEmployee;

    @Inject
    @DataField
    private Button saveButton;

    @Inject
    @DataField
    private Button cancelButton;

    @Inject
    @DataField
    private Button closeButton;

    @Inject
    @DataField
    private @Named(value = "h3") HeadingElement title;

    @Inject
    private TranslationService CONSTANTS;

    private static ShiftDrawable myShift;

    private FormPopup popup;
    private ShiftDrawable shift;
    private List<Employee> employeeList;

    public TemplateShiftEditForm() {
        shift = myShift;
    }

    public static TemplateShiftEditForm create(SyncBeanManager beanManager, ShiftDrawable shift) {
        myShift = shift;
        return beanManager.lookupBean(TemplateShiftEditForm.class).newInstance();
    }

    @PostConstruct
    protected void initWidget() {
        hasPreferredEmployee.setValue(shift.getData().getRotationEmployee() != null);
        title.setInnerSafeHtml(new SafeHtmlBuilder().appendEscaped(shift.getData().getShift().getSpot().getName())
                                                    .appendEscaped(" - ")
                                                    .appendEscaped(shift.getData().getStartTime().toLocalTime().toString())
                                                    .appendEscaped("-")
                                                    .appendEscaped(shift.getData().getEndTime().toLocalTime().toString())
                                                    .toSafeHtml());
        TemplateShiftEditForm form = this;
        EmployeeRestServiceBuilder.getEmployeeList(shift.getData().getShift().getTenantId(),
                                                   new FailureShownRestCallback<List<Employee>>() {

                                                       @Override
                                                       public void onSuccess(List<Employee> tenantEmployeeList) {
                                                           employeeList = tenantEmployeeList;
                                                           employeeList.forEach((e) -> preferredEmployee.addItem(e.getName()));
                                                           if (!hasPreferredEmployee.getValue()) {
                                                               preferredEmployee.setEnabled(false);
                                                           } else {
                                                               preferredEmployee.setSelectedIndex(employeeList.indexOf(shift.getData()
                                                                                                                            .getRotationEmployee()));
                                                           }
                                                           hasPreferredEmployee.addValueChangeHandler((v) -> preferredEmployee.setEnabled(v.getValue()));

                                                           popup = FormPopup.getFormPopup(form);
                                                           popup.center();
                                                       }
                                                   });
    }

    @EventHandler("cancelButton")
    public void cancel(ClickEvent e) {
        popup.hide();
    }

    @EventHandler("closeButton")
    public void close(ClickEvent e) {
        popup.hide();
    }

    @EventHandler("saveButton")
    public void save(ClickEvent click) {
        if (hasPreferredEmployee.getValue()) {
            Employee employee = employeeList.stream().filter((e) -> e.getName().equals(preferredEmployee.getSelectedValue())).findFirst().get();
            shift.getData().setRotationEmployee(employee);
        } else {
            shift.getData().setRotationEmployee(null);
        }
        popup.hide();
    }
}
