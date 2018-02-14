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

import elemental2.dom.HTMLDivElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListElementView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;

@Templated
public class LaneView<T> implements ListElementView<Lane<T>> {

    @Inject
    @DataField("title")
    public HTMLDivElement title;

    @Inject
    private ListView<SubLane<T>> subLanes;

    @Inject
    private ManagedInstance<SubLaneView<T>> subLaneViewInstances;

    private Lane<T> lane;

    private Viewport<T> viewport;

    @Override
    public ListElementView<Lane<T>> setup(final Lane<T> lane,
                                          final ListView<Lane<T>> list) {

        this.lane = lane;

        subLanes.init(getElement(), lane.getSubLanes(), () -> subLaneViewInstances.get()
                .withViewport(viewport)
                .withParent(lane, list));

        viewport.drawGridLinesAt(this);

        title.textContent = lane.getTitle();

        return this;
    }

    @Override
    public void destroy() {
        subLanes.clear();
    }

    public LaneView<T> withViewport(final Viewport<T> viewport) {
        this.viewport = viewport;
        return this;
    }
}
