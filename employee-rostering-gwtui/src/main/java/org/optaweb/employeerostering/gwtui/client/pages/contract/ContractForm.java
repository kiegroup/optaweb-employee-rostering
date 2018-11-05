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

package org.optaweb.employeerostering.gwtui.client.pages.contract;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.Event;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.AbstractFormPopup;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.NullableIntegerElement;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.contract.ContractRestServiceBuilder;

@Templated
public class ContractForm extends AbstractFormPopup {

    @DataField("contract-name-text-box")
    private HTMLInputElement contractName;

    @DataField("save-button")
    private HTMLButtonElement saveContractButton;

    @DataField("maximum-minutes-per-day")
    private NullableIntegerElement maximumMinutesPerDay;

    @DataField("maximum-minutes-per-week")
    private NullableIntegerElement maximumMinutesPerWeek;

    @DataField("maximum-minutes-per-month")
    private NullableIntegerElement maximumMinutesPerMonth;

    @DataField("maximum-minutes-per-year")
    private NullableIntegerElement maximumMinutesPerYear;

    private TranslationService translationService;

    private EventManager eventManager;

    private TenantStore tenantStore;

    private Contract contract;

    @Inject
    public ContractForm(PopupFactory popupFactory, HTMLDivElement root, @Named("span") HTMLElement popupTitle,
                        HTMLButtonElement closeButton, HTMLButtonElement cancelButton,
                        HTMLInputElement contractName, HTMLButtonElement saveContractButton,
                        NullableIntegerElement maximumMinutesPerDay, NullableIntegerElement maximumMinutesPerWeek,
                        NullableIntegerElement maximumMinutesPerMonth, NullableIntegerElement maximumMinutesPerYear,
                        TranslationService translationService, EventManager eventManager, TenantStore tenantStore) {
        super(popupFactory, root, popupTitle, closeButton, cancelButton);
        this.contractName = contractName;
        this.saveContractButton = saveContractButton;
        this.maximumMinutesPerDay = maximumMinutesPerDay;
        this.maximumMinutesPerWeek = maximumMinutesPerWeek;
        this.maximumMinutesPerMonth = maximumMinutesPerMonth;
        this.maximumMinutesPerYear = maximumMinutesPerYear;
        this.translationService = translationService;
        this.eventManager = eventManager;
        this.tenantStore = tenantStore;
    }

    @PostConstruct
    public void init() {
        contractName.pattern = ".+";
        contractName.required = true;
        maximumMinutesPerDay.getValueInput().min = "1";
        maximumMinutesPerWeek.getValueInput().min = "1";
        maximumMinutesPerMonth.getValueInput().min = "1";
        maximumMinutesPerYear.getValueInput().min = "1";

        listenForEvent("change");
        listenForEvent("keydown");
        listenForEvent("keypress");
        listenForEvent("keyup");
    }

    private void listenForEvent(String eventName) {
        contractName.addEventListener(eventName, this::onFormInputChange);
        maximumMinutesPerDay.getValueInput().addEventListener(eventName, this::onFormInputChange);
        maximumMinutesPerWeek.getValueInput().addEventListener(eventName, this::onFormInputChange);
        maximumMinutesPerMonth.getValueInput().addEventListener(eventName, this::onFormInputChange);
        maximumMinutesPerYear.getValueInput().addEventListener(eventName, this::onFormInputChange);
    }

    public void createNewContract() {
        setTitle(translationService.format(I18nKeys.ContractForm_createContract));
        setup(null, null, null, null);
    }

    public void editContract(Contract contract) {
        setTitle(translationService.format(I18nKeys.ContractForm_editContract));
        this.contract = contract;
        this.contractName.value = contract.getName();
        setup(contract.getMaximumMinutesPerDay(), contract.getMaximumMinutesPerWeek(), contract.getMaximumMinutesPerMonth(), contract.getMaximumMinutesPerYear());
    }

    private void setup(Integer maximumMinutesPerDay, Integer maximumMinutesPerWeek, Integer maximumMinutesPerMonth, Integer maximumMinutesPerYear) {
        this.maximumMinutesPerDay.setup(maximumMinutesPerDay, translationService.format(I18nKeys.ContractForm_perDay));
        this.maximumMinutesPerWeek.setup(maximumMinutesPerWeek, translationService.format(I18nKeys.ContractForm_perWeek));
        this.maximumMinutesPerMonth.setup(maximumMinutesPerMonth, translationService.format(I18nKeys.ContractForm_perMonth));
        this.maximumMinutesPerYear.setup(maximumMinutesPerYear, translationService.format(I18nKeys.ContractForm_perYear));
        saveContractButton.disabled = !isValid();
        show();
    }

    @Override
    protected void onClose() {
        // No cleanup needed on form close
    }

    @EventHandler("save-button")
    public void onApplyButtonClick(@ForEvent("click") final MouseEvent e) {
        e.stopPropagation();
        if (isValid()) {
            if (contract != null) {
                contract.setName(contractName.value);
                contract.setMaximumMinutesPerDay(maximumMinutesPerDay.getValue().orElse(null));
                contract.setMaximumMinutesPerWeek(maximumMinutesPerWeek.getValue().orElse(null));
                contract.setMaximumMinutesPerMonth(maximumMinutesPerMonth.getValue().orElse(null));
                contract.setMaximumMinutesPerYear(maximumMinutesPerYear.getValue().orElse(null));
                ContractRestServiceBuilder.updateContract(tenantStore.getCurrentTenantId(), contract, FailureShownRestCallback.onSuccess(c -> {
                    hide();
                    eventManager.fireEvent(EventManager.Event.DATA_INVALIDATION, Contract.class);
                }));
            } else {
                contract = new Contract(tenantStore.getCurrentTenantId(), contractName.value,
                                        maximumMinutesPerDay.getValue().orElse(null),
                                        maximumMinutesPerWeek.getValue().orElse(null),
                                        maximumMinutesPerMonth.getValue().orElse(null),
                                        maximumMinutesPerYear.getValue().orElse(null));
                ContractRestServiceBuilder.addContract(tenantStore.getCurrentTenantId(), contract, FailureShownRestCallback.onSuccess(c -> {
                    hide();
                    eventManager.fireEvent(EventManager.Event.DATA_INVALIDATION, Contract.class);
                }));
            }
        }
    }

    // Exploits event bubbling
    @EventHandler("form")
    public void onFormInputChange(@ForEvent("change") Event e) {
        saveContractButton.disabled = !isValid();
    }

    private boolean isValid() {
        return contractName.reportValidity() && maximumMinutesPerDay.reportValidity() && maximumMinutesPerWeek.reportValidity() && maximumMinutesPerMonth.reportValidity() && maximumMinutesPerYear.reportValidity();
    }
}
