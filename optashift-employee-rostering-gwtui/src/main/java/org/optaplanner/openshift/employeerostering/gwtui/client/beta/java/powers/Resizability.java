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

public class Resizability<T> {

    private Blob<T> blob;
    private Viewport<T> viewport;
    private IsElement blobView;
    private SubLaneView<T> subLaneView;
    private Function<Long, Boolean> onResize;

    public void applyFor(final IsElement blobView,
                         final SubLaneView<T> subLaneView,
                         final Viewport<T> viewport,
                         final Blob<T> blob) {

        this.blobView = blobView;
        this.subLaneView = subLaneView;
        this.viewport = viewport;
        this.blob = blob;

        makeResizable(blobView.getElement(),
                      viewport.gridPixelSizeInScreenPixels.intValue(),
                      viewport.decideBasedOnOrientation("s", "e"));
    }

    private native void makeResizable(final HTMLElement blob,
                                      final int pixelSize,
                                      final String orientation) /*-{
        var that = this;
        var $blob = $wnd.jQuery(blob);

        var snapToGrid = function (coordinate) {
            return Math.floor(coordinate - (coordinate % pixelSize))
        };

        $blob.resizable({
            handles: orientation,
            minHeight: 0,
            resize: function (e, ui) {
                if (orientation === 's') {
                    ui.size.height = snapToGrid(ui.size.height + 2 * pixelSize);
                } else if (orientation === 'e') {
                    ui.size.width = snapToGrid(ui.size.width + 2 * pixelSize);
                }
                that.@org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.Resizability::onResize(II)(ui.size.height, ui.size.width);
            }
        });
    }-*/;

    private boolean onResize(final int height, final int width) {
        final Long newSizeInGridPixels = viewport.toGridPixels(viewport.decideBasedOnOrientation(height, width).longValue());
        final Long originalSize = blob.getSizeInGridPixels();
        blobView.getElement().style.backgroundColor = "";

        if (!newSizeInGridPixels.equals(originalSize)) {
            blob.setSizeInGridPixels(newSizeInGridPixels);
            if (!subLaneView.hasSpaceForIgnoring(blob, blob)) {
                blob.setSizeInGridPixels(originalSize);
                blobView.getElement().style.backgroundColor = "red";
                DomGlobal.console.info("Collision!"); //TODO: Restrict resizing if a collision occurs.
                return false;
            } else {
                blob.setSizeInGridPixels(originalSize);
                return onResize.apply(newSizeInGridPixels);
            }
        }

        return false;
    }

    public void onResize(final Function<Long, Boolean> onResize) {
        this.onResize = onResize;
    }
}
