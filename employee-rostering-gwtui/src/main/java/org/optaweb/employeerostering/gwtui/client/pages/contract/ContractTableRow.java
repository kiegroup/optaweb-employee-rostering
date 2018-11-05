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

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.contract.ContractRestServiceBuilder;

@Templated("#row")
public class ContractTableRow implements TakesValue<Contract>,
                                         IsElement {

    private Contract contract;

    @Inject
    @DataField("contract-name")
    @Named("td")
    private HTMLTableCellElement contractName;

    @Inject
    @DataField("delete")
    private HTMLAnchorElement deleteContractButton;
    @Inject
    @DataField("edit")
    private HTMLAnchorElement editContractButton;

    @Inject
    private EventManager eventManager;

    @Inject
    private ManagedInstance<ContractForm> contractFormFactory;

    @Inject
    private TenantStore tenantStore;

    @Override
    public void setValue(Contract contract) {
        this.contract = contract;
        contractName.innerHTML = new SafeHtmlBuilder().appendEscaped(contract.getName()).toSafeHtml().asString();
    }

    @Override
    public Contract getValue() {
        return contract;
    }

    @EventHandler("edit")
    public void onEditContractButtonClick(@ForEvent("click") MouseEvent e) {
        contractFormFactory.get().editContract(contract);
    }

    @EventHandler("delete")
    public void onDeleteContractButtonClick(@ForEvent("click") MouseEvent e) {
        ContractRestServiceBuilder.removeContract(tenantStore.getCurrentTenantId(), contract.getId(), FailureShownRestCallback.onSuccess(v -> {
            eventManager.fireEvent(EventManager.Event.DATA_INVALIDATION, Contract.class);
        }));
    }
}
