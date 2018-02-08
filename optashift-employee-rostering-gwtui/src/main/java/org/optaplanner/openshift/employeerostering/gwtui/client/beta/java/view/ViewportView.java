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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.MouseEvent;
import jsinterop.base.Js;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;

@Templated("ViewportView.html")
public class ViewportView<T> implements IsElement {

    @Inject
    @DataField("viewport")
    public HTMLDivElement root;

    @Inject
    private ManagedInstance<LaneView<T>> laneViewInstances;

    @Inject
    private ListView<Lane<T>> lanes;

    private Viewport<T> viewport;

    @EventHandler("viewport")
    public void onClick(final ClickEvent event) {

        final MouseEvent e = Js.cast(event.getNativeEvent());

        if (!e.target.equals(e.currentTarget)) {
            return;
        }

        // Add Lane (SHIFT + CLICK)
        if (e.shiftKey) {
            final List<SubLane<T>> subLanes = new ArrayList<>();
            final SubLane<T> subLane = new SubLane<>(new ArrayList<>());
            subLanes.add(subLane);
            lanes.add(new Lane<>("New", subLanes));
        }
    }

    public void setViewport(final Viewport<T> viewport) {
        this.viewport = viewport;
        getElement().classList.add(viewport.decideBasedOnOrientation("vertical", "horizontal"));

        viewport.setSizeInScreenPixels(this, viewport.getSizeInGridPixels(), 12L);

        lanes.init(getElement(), viewport.getLanes(), () -> laneViewInstances.get()
                .withViewport(viewport));
    }
}
