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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.rotation;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import jsinterop.base.Js;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListElementView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.Draggability;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.Resizability;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.BlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.SubLaneView;

@Templated
public class ShiftBlobView implements BlobView<Long, ShiftBlob> {

    @Inject
    @DataField("blob")
    private HTMLDivElement root;

    @Inject
    @Named("span")
    @DataField("label")
    private HTMLElement label;

    @Inject
    private Draggability<Long> draggability;

    @Inject
    private Resizability<Long> resizability;

    private Viewport<Long> viewport;
    private SubLaneView<Long> subLaneView;
    private ListView<ShiftBlob> list;

    private ShiftBlob blob;

    @Override
    public ListElementView<ShiftBlob> setup(final ShiftBlob blob, final ListView<ShiftBlob> list) {

        this.blob = blob;
        this.list = list;

        viewport.setPositionInScreenPixels(this, viewport.getScale().toGridPixels(this.blob.getPosition()), 0L);
        viewport.setSizeInScreenPixels(this, this.blob.getSizeInGridPixels(), 0L);
        updateLabel();

        draggability.onDrag(this::onDrag);
        draggability.applyFor(this, subLaneView, viewport, blob);

        resizability.onResize(this::onResize);
        resizability.applyFor(this, subLaneView, viewport, blob);

        return this;
    }

    private void updateLabel() {
        final String start = (blob.getPosition() / 60) % 24 + ":00";
        final String end = (blob.getEndPosition(viewport.getScale()) / 60) % 24 + ":00";
        label.textContent = start + " to " + end;
    }

    private boolean onResize(final Long newSizeInGridPixels) {
        blob.setSizeInGridPixels(newSizeInGridPixels);
        updateLabel();
        return true;
    }

    private boolean onDrag(final Long newPositionInGridPixels) {
        blob.setPosition(viewport.getScale().toScaleUnits(newPositionInGridPixels));
        updateLabel();
        return true;
    }

    @EventHandler("blob")
    public void onBlobClicked(final ClickEvent event) {
        final MouseEvent e = Js.cast(event.getNativeEvent());

        if (e.altKey) {
            list.remove(blob);
        }
    }

    @Override
    public BlobView<Long, ShiftBlob> withViewport(final Viewport<Long> viewport) {
        this.viewport = viewport;
        return this;
    }

    @Override
    public BlobView<Long, ShiftBlob> withSubLaneView(final SubLaneView<Long> subLaneView) {
        this.subLaneView = subLaneView;
        return this;
    }
}
