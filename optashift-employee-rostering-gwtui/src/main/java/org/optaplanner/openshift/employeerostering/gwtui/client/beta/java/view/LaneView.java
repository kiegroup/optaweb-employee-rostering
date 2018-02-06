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

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view;

import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListElementView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListView;

@Templated
public class LaneView implements ListElementView<Lane> {

    @Inject
    private ListView<SubLane> subLanes;

    @Inject
    private ManagedInstance<SubLaneView> subLaneViewInstances;

    private Lane lane;

    private Viewport viewport;

    @Override
    public ListElementView<Lane> setup(Lane lane, ListView<Lane> list) {
        subLanes.init(getElement(), lane.getSubLanes(), () -> subLaneViewInstances.get().withViewport(viewport));
        this.lane = lane;
        return this;
    }

    public LaneView withViewport(final Viewport viewport) {
        this.viewport = viewport;
        return this;
    }
}
