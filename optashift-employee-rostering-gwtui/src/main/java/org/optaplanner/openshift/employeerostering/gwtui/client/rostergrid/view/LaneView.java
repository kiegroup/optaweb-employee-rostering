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

package org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view;

import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListElementView;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;

@Templated
public class LaneView<T> implements ListElementView<Viewport<T>, Lane<T>> {

    @Inject
    @DataField("title")
    public HTMLDivElement title;

    @Inject
    private ListView<Lane<T>, SubLane<T>> subLanes;

    @Inject
    private ManagedInstance<SubLaneView<T>> subLaneViewInstances;

    @Inject
    @Named("span")
    private HTMLElement laneBackground;

    private Lane<T> lane;

    private Viewport<T> viewport;

    @Override
    public ListElementView<Viewport<T>, Lane<T>> setup(final Lane<T> lane,
                                                       final ListView<Viewport<T>, Lane<T>> laneViews) {

        this.lane = lane;

        subLaneViewInstances.get().withViewport(viewport).withParent(lane, laneViews);
        subLanes.init(laneViews.getHTMLParentElement(), lane, lane.getSubLanes(), () -> subLaneViewInstances.get()
                .withViewport(viewport)
                .withParent(lane, laneViews));

        getElement().textContent = lane.getTitle();
        laneBackground.classList.add("lane-background");

        viewport.setAbsPositionInScreenPixels(() -> laneBackground, 1L, 0L);
        viewport.setSizeInScreenPixels(() -> laneBackground, viewport.getScale().getEndInGridPixels(), 0L);
        viewport.setGroupPosition(() -> laneBackground, viewport.getLaneStartPosition(lane));
        viewport.setGroupSizeInScreenPixels(() -> laneBackground, lane.getSubLanes().size() + 0L, 0L);

        viewport.setGroupPosition(this, viewport.getLaneStartPosition(lane));
        viewport.setGroupSizeInScreenPixels(this, lane.getSubLanes().size() + 0L, 0L);

        laneViews.getHTMLParentElement().appendChild(laneBackground);
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
