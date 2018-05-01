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

import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListElementView;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.CircularDraggability;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.CircularResizability;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.CollisionState;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;
import org.slf4j.Logger;

import static org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.CollisionState.COLLIDING;

@Templated
public class ShiftTemplateBlobView implements BlobView<LocalDateTime, ShiftTemplateBlob> {

    private static final Long BLOB_POSITION_DISPLACEMENT_IN_SCREEN_PIXELS = 3L;
    private static final Long BLOB_SIZE_DISPLACEMENT_IN_SCREEN_PIXELS = -5L;

    @Inject
    @DataField("blob")
    private HTMLDivElement root;

    @Inject
    @Named("span")
    @DataField("label")
    private HTMLElement label;

    @Inject
    private CircularDraggability<LocalDateTime, ShiftTemplateBlob> draggability;

    @Inject
    private CircularResizability<LocalDateTime, ShiftTemplateBlob> resizability;

    @Inject
    private Logger logger;

    private Viewport<LocalDateTime> viewport;
    private SubLane<LocalDateTime> subLane;
    private ListView<ShiftTemplateBlob> blobViews;
    private Runnable onDestroy;

    private ShiftTemplateBlob blob;

    @Override
    public ListElementView<ShiftTemplateBlob> setup(final ShiftTemplateBlob blob,
                                                    final ListView<ShiftTemplateBlob> blobViews) {

        this.blob = blob;
        this.blobViews = blobViews;

        refresh();

        draggability.applyFor(blob, BLOB_POSITION_DISPLACEMENT_IN_SCREEN_PIXELS, blobViews, subLane.getCollisionDetector(), viewport, this);
        draggability.onDrag(this::onDrag);

        resizability.applyFor(blob, BLOB_SIZE_DISPLACEMENT_IN_SCREEN_PIXELS, blobViews, subLane.getCollisionDetector(), viewport, this);
        resizability.onResize(this::onResize);

        return this;
    }

    private void refresh() {
        positionBlobOnGrid();
        updateLabel();
    }

    private void positionBlobOnGrid() {
        viewport.setPositionInScreenPixels(this, blob.getPositionInGridPixels(), BLOB_POSITION_DISPLACEMENT_IN_SCREEN_PIXELS);
        viewport.setSizeInScreenPixels(this, blob.getSizeInGridPixels(), BLOB_SIZE_DISPLACEMENT_IN_SCREEN_PIXELS);
    }

    private void updateLabel() {
        label.textContent = (blob.getShiftTemplateView().getRotationEmployeeId() != null) ? blob.getRotationEmployee().getName() : "Unassigned";
    }

    private void onResize(final double newSizeInGridPixels,
                          final CollisionState dragState) {

        refresh();
        refreshTwinIfAny();

        if (dragState.equals(COLLIDING)) {
            logger.info("Collision!");
        }
    }

    private void onDrag(final double newPositionInGridPixels,
                        final CollisionState dragState) {

        refresh();
        refreshTwinIfAny();

        if (dragState.equals(COLLIDING)) {
            logger.info("Collision!");
        }
    }

    private void refreshTwinIfAny() {
        blob.getTwin()
                .map(blobViews::getView)
                .map(view -> (ShiftTemplateBlobView) view)
                .ifPresent(ShiftTemplateBlobView::refresh);
    }

    @EventHandler("blob")
    public void onBlobClicked(final @ForEvent("click") MouseEvent e) {
        if (e.altKey) {
            blob.getTwin().ifPresent(twin -> {
                blobViews.remove(twin);
                blob.setTwin(null);
            });
            blobViews.remove(blob);
            //FIXME: Remove SubLane if list is empty
        }
    }

    @Override
    public BlobView<LocalDateTime, ShiftTemplateBlob> withViewport(final Viewport<LocalDateTime> viewport) {
        this.viewport = viewport;
        return this;
    }

    @Override
    public BlobView<LocalDateTime, ShiftTemplateBlob> withSubLane(final SubLane<LocalDateTime> subLane) {
        this.subLane = subLane;
        return this;
    }

    @Override
    public void onDestroy(final Runnable onDestroy) {
        this.onDestroy = onDestroy;
        getElement().style.backgroundColor = ""; //FIXME: Remove this after collision is properly handled
    }

    @Override
    public void destroy() {
        onDestroy.run();
    }

    @Override
    public Blob<LocalDateTime> getBlob() {
        return blob;
    }
}
