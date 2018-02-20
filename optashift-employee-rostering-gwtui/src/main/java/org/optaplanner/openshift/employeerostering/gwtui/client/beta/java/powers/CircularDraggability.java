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
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.SubLaneView;

public class Draggability<T> {

    private IsElement blobView;
    private Blob<T> blob;
    private SubLaneView<T> subLaneView;
    private Viewport<T> viewport;
    private Function<Long, Boolean> onDrag;

    public void applyFor(final IsElement blobView,
                         final SubLaneView<T> subLaneView,
                         final Viewport<T> viewport,
                         final Blob<T> blob) {

        this.blobView = blobView;
        this.blob = blob;
        this.subLaneView = subLaneView;
        this.viewport = viewport;

        makeDraggable(blobView.getElement(),
                      subLaneView.getElement(),
                      viewport.getGridPixelSizeInScreenPixels().intValue(),
                      viewport.decideBasedOnOrientation("y", "x"));
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
        final Long newPositionInGridPixels = viewport.toGridPixels(viewport.decideBasedOnOrientation(top, left).longValue());
        final T originalPosition = blob.getPosition();
        final LinearScale<T> scale = viewport.getScale();
        blobView.getElement().style.backgroundColor = "";

        if (!newPositionInGridPixels.equals(scale.toGridPixels(originalPosition))) {
            blob.setPosition(scale.toScaleUnits(newPositionInGridPixels));
            if (!subLaneView.hasSpaceForIgnoring(blob, blob)) {
                blob.setPosition(originalPosition);
                blobView.getElement().style.backgroundColor = "red";
                DomGlobal.console.info("Collision!"); //TODO: Restrict dragging if a collision occurs.
                return false;
            } else {
                blob.setPosition(originalPosition);
                onDrag.apply(newPositionInGridPixels);
            }
        }

        return false;
    }

    public void onDrag(final Function<Long, Boolean> onDrag) {
        this.onDrag = onDrag;
    }
}
