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

package org.optaweb.employeerostering.gwtui.client.tenant;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.shared.tenant.Tenant;
import org.optaweb.employeerostering.shared.tenant.TenantRestServiceBuilder;

@Templated("#row")
public class TenantTableRow implements TakesValue<Tenant>,
                                       IsElement {

    private Tenant tenant;

    @Inject
    @DataField("tenant-name")
    @Named("td")
    private HTMLTableCellElement tenantName;

    @Inject
    @DataField("delete-tenant-button")
    private HTMLButtonElement deleteTenantButton;

    @Inject
    private EventManager eventManager;

    @Override
    public void setValue(Tenant tenant) {
        this.tenant = tenant;
        tenantName.innerHTML = new SafeHtmlBuilder().appendEscaped(tenant.getName()).toSafeHtml().asString();
    }

    @Override
    public Tenant getValue() {
        return tenant;
    }

    @EventHandler("delete-tenant-button")
    public void onDeleteTenantButtonClick(@ForEvent("click") MouseEvent e) {
        TenantRestServiceBuilder.removeTenant(tenant.getId(), FailureShownRestCallback.onSuccess(v -> {
            eventManager.fireEvent(EventManager.Event.DATA_INVALIDATION, Tenant.class);
        }));
    }
}
