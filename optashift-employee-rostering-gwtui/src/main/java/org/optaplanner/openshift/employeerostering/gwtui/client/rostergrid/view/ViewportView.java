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

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MutationObserver;
import elemental2.dom.MutationObserver.MutationObserverCallbackFn;
import elemental2.dom.MutationObserverInit;
import elemental2.dom.Node;
import jsinterop.base.Js;
import jsinterop.base.JsArrayLike;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.TimingUtils;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

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

    private MutationObserverInit domObserverConfig;

    // In an ideal world we wouldn't need these, but alas this isn't an ideal world
    private static native MutationObserverInit getMutationObserverInit() /*-{
        return {subtree: true, childList: true};
    }-*/;
    
    private static native MutationObserver getMutationObserver(MutationObserverCallbackFn callback)/*-{
        return new MutationObserver(callback);
    }-*/;

    @PostConstruct
    private void init() {
        domObserverConfig = getMutationObserverInit();
    }

    public void setViewport(final Viewport<T> viewport) {
        timingUtils.time("Viewport assemble", () -> {

            final String orientation = viewport.decideBasedOnOrientation("vertical", "horizontal");

            dateTicksLane.classList.remove("vertical", "horizontal");
            dateTicksLane.classList.add(orientation);

            timeTicksLane.classList.remove("vertical", "horizontal");
            timeTicksLane.classList.add(orientation);

            getElement().classList.add(orientation);

            if (domObserver != null) {
                domObserver.disconnect();
            }

            domObserver = getMutationObserver((records, mutationObserver) -> {
                addOrientation(orientation, stream(records)
                        .flatMap(record -> stream(asArray(record.addedNodes)))
                        .collect(toList()));
                return null;
            });

            domObserver.observe(getElement(), domObserverConfig);

            viewport.setSizeInScreenPixels(this, viewport.getSizeInGridPixels(), 12L);

            viewport.drawDateTicksAt(() -> dateTicksLane);
            viewport.drawTimeTicksAt(() -> timeTicksLane);

            lanes.init(getElement(), viewport.getLanes(), () -> laneViewInstances.get().withViewport(viewport));
        });
    }
    private void addOrientation(final String orientation, final List<Node> nodes) {
        nodes.stream()
                .filter(node -> node instanceof HTMLElement)
                .map(node -> (HTMLElement) node)
                .peek(element -> element.classList.add(orientation))
                .map(element -> Arrays.asList(asArray(element.childNodes)))
                .forEach(childNodes -> addOrientation(orientation, childNodes));
    }

    private Node[] asArray(final JsArrayLike<Node> jsArrayLike) {
        return Js.uncheckedCast(jsArrayLike);
}
}
