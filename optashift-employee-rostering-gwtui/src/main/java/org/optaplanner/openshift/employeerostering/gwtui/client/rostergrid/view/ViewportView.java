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

import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import elemental2.dom.MouseEventInit;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PageUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.TimingUtils;

@Templated
public class ViewportView<T> implements IsElement {

    @Inject
    private ManagedInstance<LaneView<T>> laneViewInstances;

    @Inject
    private ListView<Viewport<T>, Lane<T>> lanes;

    @Inject
    private TimingUtils timingUtils;

    @Inject
    @Named("span")
    private HTMLElement headerBackground;

    @Inject
    private PageUtils pageUtils;

    private Viewport<T> viewport;

    private boolean inMouseEvent = false;

    private static int COLUMN_WIDTH_IN_PIXELS = 20;

    public void setViewport(final Viewport<T> viewport) {

        timingUtils.time("Viewport assemble", () -> {
            this.viewport = viewport;
            if (headerBackground.isConnected) {
                headerBackground.remove();
            }
            getElement().classList.add(viewport.decideBasedOnOrientation("vertical", "horizontal"));

            getElement().style.set("grid-template-columns", "auto " + "repeat(" + (viewport.getScale().getEndInGridPixels()) + ", " + COLUMN_WIDTH_IN_PIXELS + "px)");

            headerBackground.classList.add("header-background");
            getElement().appendChild(headerBackground);
            viewport.setAbsPositionInScreenPixels(() -> headerBackground, 0L);
            viewport.setSizeInScreenPixels(() -> headerBackground, viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns());
            viewport.setAbsGroupPosition(() -> headerBackground, 0);
            viewport.setGroupSizeInScreenPixels(() -> headerBackground, viewport.getHeaderRows());
            viewport.drawTicksAt(this);
            viewport.drawGridLinesAt(this);

            lanes.init(getElement(), viewport, viewport.getLanes(), () -> laneViewInstances.get().withViewport(viewport));

            pageUtils.makePageScrollable();
            getElement().style.set("width", (viewport.getGridPixelSizeInScreenPixels() * (viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns())) + "px");
            getElement().style.set("height", (30 * (viewport.getGroupEndPosition() + viewport.getHeaderRows()) + pageUtils.getHeightConsumed()) + "px");
        });
    }

    @EventHandler("viewport")
    private void onDragStart(@ForEvent("mousedown") MouseEvent e) {
        if (!inMouseEvent && viewport != null && viewport.getMouseTarget() != null) {
            try {
                inMouseEvent = true;
                e.target = viewport.getMouseTarget().getElement();
                MouseEvent copy = new MouseEvent(e.type, (MouseEventInit) e);
                viewport.getMouseTarget().getElement().dispatchEvent(copy);
            } finally {
                inMouseEvent = false;
            }
        }
    }

    @EventHandler("viewport")
    private void onDrag(@ForEvent("mousemove") MouseEvent e) {
        if (!inMouseEvent && viewport != null && viewport.getMouseTarget() != null) {
            try {
                inMouseEvent = true;
                e.target = viewport.getMouseTarget().getElement();
                MouseEvent copy = new MouseEvent(e.type, (MouseEventInit) e);
                viewport.getMouseTarget().getElement().dispatchEvent(copy);
            } finally {
                inMouseEvent = false;
            }
        }
    }

    @EventHandler("viewport")
    private void onDragEnd(@ForEvent("mouseup") MouseEvent e) {
        if (!inMouseEvent && viewport != null && viewport.getMouseTarget() != null) {
            try {
                inMouseEvent = true;
                e.target = viewport.getMouseTarget().getElement();
                MouseEvent copy = new MouseEvent(e.type, (MouseEventInit) e);
                viewport.getMouseTarget().getElement().dispatchEvent(copy);
            } finally {
                inMouseEvent = false;
            }
        }
    }

    @EventHandler("viewport")
    private void onMouseExit(@ForEvent("mouseleave") MouseEvent e) {
        if (!inMouseEvent && viewport != null && viewport.getMouseTarget() != null) {
            try {
                inMouseEvent = true;
                e.target = viewport.getMouseTarget().getElement();
                MouseEvent copy = new MouseEvent("mouseup", (MouseEventInit) e);
                viewport.getMouseTarget().getElement().dispatchEvent(copy);
            } finally {
                inMouseEvent = false;
            }
        }
    }

    public void onClose() {
        if (viewport != null) {
            viewport.onClose();
        }
    }
}
