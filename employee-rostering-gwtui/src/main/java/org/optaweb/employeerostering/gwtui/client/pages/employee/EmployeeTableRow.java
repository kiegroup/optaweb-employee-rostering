/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.pages.employee;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLTableCellElement;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.AutoTrimWhitespaceTextBox;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.StringListToSkillSetConverter;
import org.optaweb.employeerostering.gwtui.client.common.StringToContractConverter;
import org.optaweb.employeerostering.gwtui.client.common.TableRow;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaweb.employeerostering.shared.skill.Skill;

@Templated("#row")
public class EmployeeTableRow extends TableRow<Employee> {

    @Inject
    private TenantStore tenantStore;

    @Inject
    private StringListToSkillSetConverter skillConvertor;

    @Inject
    private StringToContractConverter contractConvertor;

    @Inject
    @DataField("employee-name-text-box")
    private AutoTrimWhitespaceTextBox employeeName;

    @Inject
    @DataField("employee-skill-proficiency-set-select")
    private MultipleSelect employeeSkillProficiencySet;

    @Inject
    @DataField("employee-contract-select")
    private Select employeeContract;

    @Inject
    @DataField("employee-name-display")
    @Named("td")
    private HTMLTableCellElement employeeNameDisplay;

    @Inject
    @DataField("employee-skill-proficiency-set-display")
    @Named("td")
    private HTMLTableCellElement employeeSkillProficiencySetDisplay;

    @Inject
    @DataField("employee-contract-display")
    @Named("td")
    private HTMLTableCellElement employeeContractDisplay;

    @Inject
    private TranslationService translationService;

    @Inject
    private EventManager eventManager;

    @PostConstruct
    protected void initWidget() {
        employeeName.getElement().setAttribute("placeholder", translationService.format(
                I18nKeys.EmployeeListPanel_employeeName));
        dataBinder.getModel().setTenantId(tenantStore.getCurrentTenantId());
        updateSkillMap(skillConvertor.getSkillMap());
        updateContractMap(contractConvertor.getContractMap());
        dataBinder.bind(employeeName, "name");
        dataBinder.bind(employeeContract, "contract", contractConvertor);
        dataBinder.bind(employeeSkillProficiencySet, "skillProficiencySet", skillConvertor);

        dataBinder.<String>addPropertyChangeHandler("name", (e) -> {
            employeeNameDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped(e.getNewValue()).toSafeHtml().asString();
        });
        dataBinder.<Contract>addPropertyChangeHandler("contract", (e) -> {
            employeeContractDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped((e.getNewValue() != null) ? e.getNewValue().getName() : "").toSafeHtml().asString();
        });
        dataBinder.<Set<Skill>>addPropertyChangeHandler("skillProficiencySet", (e) -> {
            employeeSkillProficiencySetDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped(e.getNewValue().stream().map(Skill::getName).collect(Collectors.joining(", ")))
                    .toSafeHtml().asString();
        });
        eventManager.subscribeToEventForElement(EventManager.Event.SKILL_MAP_INVALIDATION, this, this::updateSkillMap);
        eventManager.subscribeToEventForElement(EventManager.Event.CONTRACT_MAP_INVALIDATION, this, this::updateContractMap);
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

    private void updateContractMap(Map<String, Contract> contractMap) {
        employeeContract.clear();
        contractMap.forEach((name, contract) -> {
            Option option = new Option();
            option.setName(name);
            option.setValue(name);
            option.setText(name);
            employeeContract.add(option);
        });
        employeeContract.refresh();
    }

    @Override
    protected void deleteRow(Employee employee) {
        EmployeeRestServiceBuilder.removeEmployee(tenantStore.getCurrentTenantId(), employee.getId(),
                                                  FailureShownRestCallback.onSuccess(success -> {
                                                      eventManager.fireEvent(EventManager.Event.DATA_INVALIDATION, Employee.class);
                                                  }));
    }

    @Override
    protected void updateRow(Employee oldValue, Employee newValue) {

        EmployeeRestServiceBuilder.updateEmployee(tenantStore.getCurrentTenantId(), newValue,
                                                  FailureShownRestCallback.onSuccess(v -> {
                                                      eventManager.fireEvent(EventManager.Event.DATA_INVALIDATION, Employee.class);
                                                  }));
    }

    @Override
    protected void createRow(Employee employee) {
        EmployeeRestServiceBuilder.addEmployee(tenantStore.getCurrentTenantId(), employee,
                                               FailureShownRestCallback.onSuccess(v -> {
                                                   eventManager.fireEvent(EventManager.Event.DATA_INVALIDATION, Employee.class);
                                               }));
    }

    @Override
    protected void focusOnFirstInput() {
        employeeName.setFocus(true);
    }
}
