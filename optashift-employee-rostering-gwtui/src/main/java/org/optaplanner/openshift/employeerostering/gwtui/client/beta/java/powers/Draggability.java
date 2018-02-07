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

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers;

import java.util.function.Function;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.SubLaneView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport.Orientation.VERTICAL;

public class Draggability {

    private Blob blob;
    private SubLaneView subLaneView;
    private Viewport viewport;
    private Function<Integer, Boolean> onDrag;

    public void applyFor(final IsElement blobView,
                         final SubLaneView subLaneView,
                         final Viewport viewport,
                         final Blob blob) {

        this.blob = blob;
        this.subLaneView = subLaneView;
        this.viewport = viewport;

        makeDraggable(blobView.getElement(),
                      subLaneView.getElement(),
                      viewport.pixelSize,
                      viewport.orientation.equals(VERTICAL) ? "y" : "x");
    }

    private native void makeDraggable(final HTMLElement blob,
                                      final HTMLElement subLane,
                                      final int pixelSize,
                                      final String orientation) /*-{

        var that = this;
        var $blob = $wnd.jQuery(blob);
        var $subLane = $wnd.jQuery(subLane);

        $blob.draggable({
            addClasses: false,
            cancel: '.blob div',
            containment: $subLane,
            axis: orientation,
            grid: [pixelSize, pixelSize],
            drag: function (e, ui) {
                that.@org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.Draggability::onDrag(II)(ui.position.top, ui.position.left);
            },
            scroll: false
        });
    }-*/;

    private boolean onDrag(final int top, final int left) {
        final Integer newPosition = (viewport.orientation.equals(VERTICAL) ? top : left) / viewport.pixelSize;
        final Integer originalPosition = blob.getPosition();

        if (!newPosition.equals(originalPosition)) {
            if (!subLaneView.hasSpaceForIgnoring(Outline.of(newPosition, blob.getSize()), blob)) {
                DomGlobal.console.info("Collision!"); //TODO: Restrict dragging if a collision occurs.
                return false;
            } else {
                onDrag.apply(newPosition);
            }
        }

        return false;
    }

    public void onDrag(final Function<Integer, Boolean> onDrag) {
        this.onDrag = onDrag;
    }
}
