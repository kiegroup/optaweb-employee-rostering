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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.spotroster;

import java.time.LocalDateTime;

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
public class ShiftBlobView implements BlobView<LocalDateTime, ShiftBlob> {

    @Inject
    @DataField("blob")
    private HTMLDivElement root;

    @Inject
    @Named("span")
    @DataField("label")
    private HTMLElement label;

    @Inject
    private Draggability<LocalDateTime> draggability;

    @Inject
    private Resizability<LocalDateTime> resizability;

    private Viewport<LocalDateTime> viewport;
    private SubLaneView<LocalDateTime> subLaneView;
    private ListView<ShiftBlob> list;
    private Runnable onDestroy;

    private ShiftBlob blob;

    @Override
    public ListElementView<ShiftBlob> setup(final ShiftBlob blob,
                                            final ListView<ShiftBlob> list) {

        this.list = list;
        this.blob = blob;

        draggability.onDrag(this::onDrag);
        draggability.applyFor(this, subLaneView, viewport, blob);

        resizability.onResize(this::onResize);
        resizability.applyFor(this, subLaneView, viewport, blob);

        viewport.setPositionInScreenPixels(this, viewport.getScale().toGridPixels(blob.getPosition()), 0L);
        viewport.setSizeInScreenPixels(this, blob.getSizeInGridPixels(), 0L);
        updateLabel();

        return this;
    }

    private boolean onResize(final Long newSizeInGridPixels) {
        blob.setSizeInGridPixels(newSizeInGridPixels);
        //TODO: Update Shift's time slot
        //ShiftRestServiceBuilder.updateShift(blob.getShift().getTenantId(), new ShiftView(blob.getShift()));

        updateLabel();
        return true;
    }

    private boolean onDrag(final Long newPositionInGridPixels) {
        blob.setPosition(viewport.getScale().toScaleUnits(newPositionInGridPixels));
        //TODO: Update Shift's time slot
        //ShiftRestServiceBuilder.updateShift(blob.getShift().getTenantId(), new ShiftView(blob.getShift()));

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

    private void updateLabel() {
        label.textContent = blob.getLabel();
    }

    @Override
    public BlobView<LocalDateTime, ShiftBlob> withViewport(final Viewport<LocalDateTime> viewport) {
        this.viewport = viewport;
        return this;
    }

    @Override
    public BlobView<LocalDateTime, ShiftBlob> withSubLaneView(final SubLaneView<LocalDateTime> subLaneView) {
        this.subLaneView = subLaneView;
        return this;
    }

    @Override
    public void onDestroy(final Runnable onDestroy) {
        this.onDestroy = onDestroy;
    }

    @Override
    public void destroy() {
        onDestroy.run();
    }
}
