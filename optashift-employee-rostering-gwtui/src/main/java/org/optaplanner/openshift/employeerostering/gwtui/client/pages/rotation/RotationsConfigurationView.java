/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.rotation;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLSelectElement;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantConfiguration;

@Templated
public class RotationsConfigurationView implements IsElement {

    @Inject
    @DataField("week-start")
    private HTMLSelectElement weekStart;

    @Inject
    @DataField("period")
    private HTMLInputElement period;

    @Inject
    private TenantStore tenantStore;

    @PostConstruct
    public void init() {
        final TenantConfiguration configuration = tenantStore.getCurrentTenant().getConfiguration();
        weekStart.value = configuration.getWeekStart().toString();
        period.value = configuration.getTemplateDuration().toString();
    }
}
