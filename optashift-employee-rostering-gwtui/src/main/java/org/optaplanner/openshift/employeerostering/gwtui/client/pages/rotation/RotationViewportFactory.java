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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.CssGridLinesFactory;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.TicksFactory;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.FiniteLinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class RotationViewportFactory {

    @Inject
    private TenantStore tenantStore;

    @Inject
    private ManagedInstance<ShiftBlobView> shiftBlobViews;

    @Inject
    private CssGridLinesFactory cssGridLinesFactory;

    @Inject
    private TicksFactory<Long> ticksFactory;

    public Viewport<Long> getViewport(final ShiftTemplate template) {

        final Integer durationInWeeks = tenantStore.getCurrentTenant().getConfiguration().getTemplateDuration();
        final Long durationTimeInMinutes = durationInWeeks * 7 * 24 * 60L;
        final FiniteLinearScale<Long> scale = new PositiveMinutesScale(durationTimeInMinutes);

        return new RotationsViewport(tenantStore.getCurrentTenantId(),
                                     shiftBlobViews::get,
                                     scale,
                                     cssGridLinesFactory.newWithSteps(2L, 24L),
                                     ticksFactory.newTicks(scale, 4L, 24L),
                                     buildLanes(template));
    }

    public ArrayList<Lane<Long>> buildLanes(final ShiftTemplate template) {
        return new ArrayList<>(Arrays.asList(new SpotLane(new Spot(1, "New", new HashSet<>()),
                                                          new ArrayList<>(Arrays.asList(new SubLane<>(new ArrayList<>()))))));
    }
}
