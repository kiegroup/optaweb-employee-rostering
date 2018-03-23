package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.TextBox;
import elemental2.dom.HTMLTableCellElement;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.DataInvalidation;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.StringListToSkillSetConverter;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.TableRow;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;

@Templated("#row")
public class EmployeeSubform extends TableRow<Employee> implements TakesValue<Employee> {

    @Inject
    private TenantStore tenantStore;

    @Inject
    private StringListToSkillSetConverter skillConvertor;

    @Inject
    @DataField("employee-name-text-box")
    private TextBox employeeName;

    @Inject
    @DataField("employee-skill-proficiency-set-select")
    private MultipleSelect employeeSkillProficiencySet;

    @Inject
    @DataField("employee-name-display")
    @Named("td")
    private HTMLTableCellElement employeeNameDisplay;

    @Inject
    @DataField("employee-skill-proficiency-set-display")
    @Named("td")
    private HTMLTableCellElement employeeSkillProficiencySetDisplay;

    @Inject
    private Event<DataInvalidation<Employee>> dataInvalidationEvent;

    @Inject
    private TranslationService translationService;

    private Subscription subscription;

    @Inject
    private CommonUtils commonUtils;

    @SuppressWarnings("unchecked")
    @PostConstruct
    protected void initWidget() {
        employeeName.getElement().setAttribute("placeholder", translationService.format(
                OptaShiftUIConstants.EmployeeListPanel_employeeName));
        dataBinder.getModel().setTenantId(tenantStore.getCurrentTenantId());
        updateSkillMap(skillConvertor.getSkillMap());
        dataBinder.bind(employeeName, "name");
        dataBinder.bind(employeeSkillProficiencySet, "skillProficiencySet", skillConvertor);

        dataBinder.<String> addPropertyChangeHandler("name", (e) -> {
            employeeNameDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped(e.getNewValue()).toSafeHtml().asString();
        });
        dataBinder.<Set<Skill>> addPropertyChangeHandler("skillProficiencySet", (e) -> {
            employeeSkillProficiencySetDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped(commonUtils.delimitCollection(e.getNewValue(),
                    (s) -> s.getName(), ", ")).toSafeHtml().asString();
        });
        subscription = ErraiBus.get().subscribe("SkillMapListener", (m) -> updateSkillMap(m.get(Map.class, "Map")));
    }

    public void reset() {
        employeeName.setValue("");
    }

    private void updateSkillMap(Map<String, Skill> skillMap) {
        employeeSkillProficiencySet.clear();
        skillMap.forEach((name, skill) -> {
            Option option = new Option();
            option.setName(name);
            option.setValue(name);
            option.setText(name);
            employeeSkillProficiencySet.add(option);
        });
        employeeSkillProficiencySet.refresh();
    }

    @Override
    protected void deleteRow(Employee employee) {
        EmployeeRestServiceBuilder.removeEmployee(tenantStore.getCurrentTenantId(), employee.getId(),
                FailureShownRestCallback.onSuccess(success -> {
                    dataInvalidationEvent.fire(new DataInvalidation<>());
                }));
    }

    @Override
    protected void updateRow(Employee oldValue, Employee newValue) {

        EmployeeRestServiceBuilder.updateEmployee(tenantStore.getCurrentTenantId(), newValue,
                FailureShownRestCallback.onSuccess(v -> {
                    dataInvalidationEvent.fire(new DataInvalidation<>());
                }));
    }

    @Override
    protected void createRow(Employee employee) {
        EmployeeRestServiceBuilder.addEmployee(tenantStore.getCurrentTenantId(), employee,
                FailureShownRestCallback.onSuccess(v -> {
                    dataInvalidationEvent.fire(new DataInvalidation<>());
                }));
    }

    @Override
    public void onUnload() {
        subscription.remove();
    }

    @Override
    protected void focusOnFirstInput() {
        employeeName.setFocus(true);
    }
}
