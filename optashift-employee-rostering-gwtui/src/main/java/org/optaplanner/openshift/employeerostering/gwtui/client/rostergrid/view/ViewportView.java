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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MutationObserver;
import elemental2.dom.MutationObserverInit;
import elemental2.dom.MutationRecord;
import elemental2.dom.NodeList;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.TimingUtils;

@Templated
public class ViewportView<T> implements IsElement {

    @Inject
    @DataField("date-ticks-lane")
    private HTMLDivElement dateTicksLane;

    @Inject
    @DataField("time-ticks-lane")
    private HTMLDivElement timeTicksLane;

    @Inject
    private ManagedInstance<LaneView<T>> laneViewInstances;

    @Inject
    private ListView<Lane<T>> lanes;

    @Inject
    private TimingUtils timingUtils;

    private MutationObserver domObserver;

    @PostConstruct
    private void init() {
        MutationObserverInit config = new MutationObserverInit();
        config.subtree = true;
        config.childList = true;

        domObserver.observe(getElement(), config);
    }

    public void setViewport(final Viewport<T> viewport) {

        timingUtils.time("Viewport assemble", () -> {
            if (domObserver != null) {
                domObserver.disconnect();
            }
            domObserver = new MutationObserver((r, o) -> {
                for (MutationRecord record : r) {
                    NodeList<?> elements = record.addedNodes;
                    for (int i = 0; i < elements.length; i++) {
                        Object element = elements.getAt(i);
                        if (element instanceof HTMLElement) {
                            ((HTMLElement) element).classList.add(viewport.decideBasedOnOrientation("vertical", "horizontal"));
                        }
                    }
                }
                return null;
            });
            getElement().classList.add(viewport.decideBasedOnOrientation("vertical", "horizontal"));

            viewport.setSizeInScreenPixels(this, viewport.getSizeInGridPixels(), 12L);

            viewport.drawDateTicksAt(() -> dateTicksLane);
            viewport.drawTimeTicksAt(() -> timeTicksLane);

            lanes.init(getElement(), viewport.getLanes(), () -> laneViewInstances.get().withViewport(viewport));
        });
    }
}
