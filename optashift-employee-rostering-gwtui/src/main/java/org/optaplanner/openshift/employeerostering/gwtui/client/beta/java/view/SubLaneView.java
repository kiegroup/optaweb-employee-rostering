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

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport.Orientation.VERTICAL;

@Templated
public class SubLaneView implements ListElementView<SubLane> {

    @Inject
    @DataField("sub-lane")
    public HTMLDivElement root;

    @Inject
    private ListView<Blob> blobs;

    private SubLane subLane;
    private ListView<SubLane> list;

    private Viewport viewport;

    private Lane parentLane;
    private ListView<Lane> parentList;

    @Override
    @SuppressWarnings("unchecked")
    public ListElementView<SubLane> setup(final SubLane subLane,
                                          final ListView<SubLane> list) {

        this.list = list;
        blobs.init(getElement(), subLane.getBlobs(), () -> (BlobView) viewport.newBlobView().withViewport(viewport)); //FIXME: Generics issue
        this.subLane = subLane;
        return this;
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
            list.addAfter(subLane, new SubLane(new ArrayList<>()));
        }

        // Add Blob (ALT + CLICK)
        else if (e.altKey) {
            final double position = viewport.orientation.equals(VERTICAL) ? e.offsetY : e.offsetX;
            blobs.add(viewport.newBlob(new Double(position / viewport.pixelSize).intValue()));
        }
    }

    public SubLaneView withViewport(final Viewport viewport) {
        this.viewport = viewport;
        return this;
    }

    public ListElementView<SubLane> withParent(final Lane parentLane,
                                               final ListView<Lane> parentList) {

        this.parentLane = parentLane;
        this.parentList = parentList;
        return this;
    }
}
