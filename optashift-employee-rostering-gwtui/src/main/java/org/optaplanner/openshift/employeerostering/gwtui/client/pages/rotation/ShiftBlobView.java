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
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import jsinterop.base.Js;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListElementView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.CircularDraggability;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.CircularResizability;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.CollisionState;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.BlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.SubLaneView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.CollisionState.COLLIDING;

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
    private CircularDraggability<Long, ShiftBlob> draggability;

    @Inject
    private CircularResizability<Long, ShiftBlob> resizability;

    private Viewport<Long> viewport;
    private SubLaneView<Long> subLaneView;
    private ListView<ShiftBlob> list;
    private Runnable onDestroy;

    private ShiftBlob blob;

    @Override
    public ListElementView<ShiftBlob> setup(final ShiftBlob blob,
                                            final ListView<ShiftBlob> list) {

        this.blob = blob;
        this.list = list;

        refresh();

        final CollisionDetector<ShiftBlob> collisionDetector =
                (b, ignored) -> !subLaneView.hasSpaceForIgnoring(b, ignored);

        draggability.applyFor(list, this, collisionDetector, viewport, blob);
        draggability.onDrag(this::onDrag);

        resizability.applyFor(list, this, collisionDetector, viewport, blob);
        resizability.onResize(this::onResize);

        return this;
    }

    public interface CollisionDetector<T extends Blob<?>> {

        boolean checkCollisionIgnoring(final T blob, final T ignored);
    }

    private void refresh() {
        positionBlobOnGrid();
        updateLabel();
    }

    private void positionBlobOnGrid() {
        viewport.setPositionInScreenPixels(this, blob.getPositionInGridPixels(), 0L);
        viewport.setSizeInScreenPixels(this, blob.getSizeInGridPixels(), 0L);
    }

    private void updateLabel() {
        //FIXME: Bad labeling
        final String start = (blob.getPositionInScaleUnits() / 60) % 24 + ":00";
        final String end = (blob.getEndPositionInScaleUnits() / 60) % 24 + ":00";
        label.textContent = start + " to " + end;
    }

    private void onResize(final Long newSizeInGridPixels,
                          final CollisionState dragState) {

        if (dragState.equals(COLLIDING)) {
            DomGlobal.console.info("Colliding!");
        }

        refresh();
        refreshTwinIfAny();
    }

    private void onDrag(final Long newPositionInGridPixels,
                        final CollisionState dragState) {

        if (dragState.equals(COLLIDING)) {
            DomGlobal.console.info("Colliding!");
        }

        refresh();
        refreshTwinIfAny();
    }

    private void refreshTwinIfAny() {
        blob.getTwin()
                .map(list::getView)
                .map(view -> (ShiftBlobView) view)
                .ifPresent(ShiftBlobView::refresh);
    }

    @EventHandler("blob")
    public void onBlobClicked(final ClickEvent event) {
        final MouseEvent e = Js.cast(event.getNativeEvent());

        if (e.altKey) {
            list.remove(blob);
            blob.getTwin().ifPresent(twin -> {
                list.remove(twin);
                blob.setTwin(null);
            });
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

    @Override
    public void onDestroy(final Runnable onDestroy) {
        this.onDestroy = onDestroy;
    }

    @Override
    public void destroy() {
        onDestroy.run();
    }
}
