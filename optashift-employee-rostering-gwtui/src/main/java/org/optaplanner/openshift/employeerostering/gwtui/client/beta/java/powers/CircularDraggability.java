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

import java.util.function.BiConsumer;

import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.SubLaneView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.CircularDraggability.DragState.COLLIDING;
import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.CircularDraggability.DragState.NOT_COLLIDING;

public class CircularDraggability<T> {

    private ListView<Blob<T>> list;
    private IsElement blobView;
    private BlobWithTwin<T> blob;
    private SubLaneView<T> subLaneView;
    private Viewport<T> viewport;
    private BiConsumer<Long, DragState> onDrag;

    public void applyFor(final ListView<Blob<T>> list,
                         final IsElement blobView,
                         final SubLaneView<T> subLaneView,
                         final Viewport<T> viewport,
                         final BlobWithTwin<T> blob) {

        this.list = list;
        this.blobView = blobView;
        this.blob = blob;
        this.subLaneView = subLaneView;
        this.viewport = viewport;

        makeDraggable(blobView.getElement(),
                      viewport.getGridPixelSizeInScreenPixels().intValue(),
                      viewport.decideBasedOnOrientation("y", "x"));
    }

    private native void makeDraggable(final HTMLElement blob,
                                      final int pixelSize,
                                      final String orientation) /*-{

        var that = this;
        var $blob = $wnd.jQuery(blob);

        $blob.draggable({
            addClasses: false,
            cancel: '.blob div',
            axis: orientation,
            grid: [pixelSize, pixelSize],
            stop: function (e, ui) {
                that.@org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.CircularDraggability::onDragEnd(II)(ui.position.top, ui.position.left);
            },
            drag: function (e, ui) {
                that.@org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.CircularDraggability::onDrag(II)(ui.position.top, ui.position.left);
            },
            scroll: false
        });
    }-*/;

    private boolean onDragEnd(final int top, final int left) {

        if (blob.getPositionInGridPixels() >= viewport.getSizeInGridPixels() || blob.getEndPositionInGridPixels() <= 0) {
            blob.getTwin().ifPresent(twin -> twin.setTwin(null));
            list.remove(blob);
        }

        return true;
    }

    private boolean onDrag(final int top, final int left) {
        final Long newPositionInGridPixels = viewport.toGridPixels(viewport.decideBasedOnOrientation(top, left).longValue());

        if (!newPositionInGridPixels.equals(blob.getPositionInGridPixels())) {

            blob.setPositionInScaleUnits(viewport.getScale().toScaleUnits(newPositionInGridPixels));
            createOrRemoveTwin(newPositionInGridPixels);

            final boolean anyCollisionDetected =
                    !subLaneView.hasSpaceForIgnoring(blob, blob) ||
                            !blob.getTwin().map(twin -> subLaneView.hasSpaceForIgnoring(twin, twin)).orElse(true);

            if (anyCollisionDetected) {
                paintBlobsBackground("red");
                onDrag.accept(newPositionInGridPixels, COLLIDING);
            } else {
                paintBlobsBackground("");
                onDrag.accept(newPositionInGridPixels, NOT_COLLIDING);
            }
        }

        return false;
    }

    private void createOrRemoveTwin(final Long newPositionInGridPixels) {

        final boolean hasAnyPartOffTheGrid =
                blob.getEndPositionInGridPixels() > viewport.getSizeInGridPixels() ||
                        blob.getPositionInGridPixels() < 0;

        if (hasAnyPartOffTheGrid) {
            final BlobWithTwin<T> twin = blob.getTwin().orElseGet(blob::makeTwin);
            final Long offset = (newPositionInGridPixels < 0 ? 1 : -1) * viewport.getSizeInGridPixels();
            twin.setPositionInScaleUnits(viewport.getScale().toScaleUnits(newPositionInGridPixels + offset));
            list.addIfNotPresent(twin);
        } else {
            blob.getTwin().ifPresent(twin -> {
                list.remove(twin);
                blob.setTwin(null);
            });
        }
    }

    //FIXME: This is a side-effect used in development only
    private void paintBlobsBackground(final String backgroundColor) {
        blobView.getElement().style.backgroundColor = backgroundColor;
        blob.getTwin().map(list::getView).ifPresent(v -> v.getElement().style.backgroundColor = backgroundColor);
    }

    public void onDrag(final BiConsumer<Long, DragState> onDrag) {
        this.onDrag = onDrag;
    }

    public enum DragState {
        COLLIDING,
        NOT_COLLIDING;
    }
}
