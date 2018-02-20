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
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.MouseEvent;
import jsinterop.base.Js;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListElementView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;

@Templated
public class SubLaneView<T> implements ListElementView<SubLane<T>> {

    @Inject
    @DataField("sub-lane")
    public HTMLDivElement root;

    @Inject
    private ListView<Blob<T>> blobs;

    private SubLane<T> subLane;
    private ListView<SubLane<T>> list;

    private Viewport<T> viewport;

    private Lane<T> parentLane;
    private ListView<Lane<T>> parentList;

    @Override
    @SuppressWarnings("unchecked")
    public ListElementView<SubLane<T>> setup(final SubLane<T> subLane,
                                             final ListView<SubLane<T>> list) {

        this.list = list;
        this.subLane = subLane;

        //FIXME: Generics issue
        blobs.init(getElement(), subLane.getBlobs(), () -> (BlobView) viewport.newBlobView()
                .withViewport(viewport)
                .withSubLaneView(this));

        return this;
    }

    @Override
    public void destroy() {
        blobs.clear();
    }

    @EventHandler("sub-lane")
    public void onClick(final ClickEvent event) {
        final MouseEvent e = Js.cast(event.getNativeEvent());

        if (!e.target.equals(e.currentTarget)) {
            return;
        }

        // Remove SubLane (SHIFT + ALT + CLICK)
        if (e.shiftKey && e.altKey) {
            list.remove(subLane);
            if (list.isEmpty()) {
                parentList.remove(parentLane);
            }
        }

        // Add SubLane (SHIFT + CLICK)
        else if (e.shiftKey) {
            list.addAfter(subLane, new SubLane<>(new ArrayList<>()));
        }

        // Add Blob (ALT + CLICK)
        else if (e.altKey) {
            final double offset = viewport.decideBasedOnOrientation(e.offsetY, e.offsetX);
            final Long positionInGridPixels = viewport.toGridPixels(new Double(offset).longValue());
            T positionInScaleUnits = viewport.getScale().toScaleUnits(positionInGridPixels);

            final Blob<T> newBlob = viewport.newBlob(parentLane, positionInScaleUnits);
            if (hasSpaceForIgnoring(newBlob)) {
                blobs.add(newBlob);
            }
        }
    }

    @SafeVarargs
    public final boolean hasSpaceForIgnoring(final Blob<T> blob,
                                             final Blob<T>... ignoredBlobs) {

        final List<Blob> ignored = Arrays.asList(ignoredBlobs);
        return this.blobs.getObjects().stream()
                .filter(b -> !ignored.contains(b))
                .noneMatch(b -> b.collidesWith(blob));
    }

    public SubLaneView<T> withViewport(final Viewport<T> viewport) {
        this.viewport = viewport;
        return this;
    }

    public ListElementView<SubLane<T>> withParent(final Lane<T> parentLane,
                                                  final ListView<Lane<T>> parentList) {

        this.parentLane = parentLane;
        this.parentList = parentList;
        return this;
    }
}
