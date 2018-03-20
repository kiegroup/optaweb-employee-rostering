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

import java.time.OffsetDateTime;

import javax.annotation.PostConstruct;
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
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.BlobMouseHandler;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.BlobMouseHandler.BlobMouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.CollisionState;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;
import org.slf4j.Logger;

import static org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.CollisionState.COLLIDING;

@Templated
public class ShiftBlobView implements BlobView<OffsetDateTime, ShiftBlob> {

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
    private Logger logger;

    private Viewport<OffsetDateTime> viewport;
    private SubLane<OffsetDateTime> subLane;
    private ListView<SubLane<OffsetDateTime>, ShiftBlob> blobViews;
    private Runnable onDestroy;

    private BlobMouseHandler<OffsetDateTime> mouseHandler;
    private ShiftBlob blob;

    @Override
    public ListElementView<SubLane<OffsetDateTime>, ShiftBlob> setup(final ShiftBlob blob,
                                                                     final ListView<SubLane<OffsetDateTime>, ShiftBlob> blobViews) {
        this.blob = blob;
        this.blobViews = blobViews;
        this.withSubLane(blobViews.getContainer());

        refresh();

        return this;
    }

    @PostConstruct
    private void init() {
        mouseHandler = new BlobMouseHandler<OffsetDateTime>(this::getBlob, this::getViewport, this)
                .withDragHandler(this::onDrag)
                .withResizeHandler(this::onResize);
    }

    private Viewport<OffsetDateTime> getViewport() {
        return viewport;
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
        label.textContent = (blob.getShift().getRotationEmployee() != null) ? blob.getShift().getRotationEmployee().getName() : "Unassigned";
    }

    private void onResize(final Long newSizeInGridPixels,
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
                .map(view -> (ShiftBlobView) view)
                .ifPresent(ShiftBlobView::refresh);
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
    public BlobView<OffsetDateTime, ShiftBlob> withViewport(final Viewport<OffsetDateTime> viewport) {
        this.viewport = viewport;
        return this;
    }

    @Override
    public BlobView<OffsetDateTime, ShiftBlob> withSubLane(final SubLane<OffsetDateTime> subLane) {
        this.subLane = subLane;
        viewport.setGroupPosition(this, viewport.getSubLanePosition(subLane));
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
    public Blob<OffsetDateTime> getBlob() {
        return blob;
    }

    private void onDrag(BlobMouseEvent e) {
        blob.setPositionInScaleUnits(viewport.getScale().toScaleUnits(Math.round(e.getMousePositionDeltaInGridPixels() + e.getOriginalBlobStartPositionInGridPixels())));
        updateView();
    }

    private void onResize(BlobMouseEvent e) {
        switch (e.getResizingFrom()) {
            case END:
                if (e.getMousePositionInGridPixels() <= e.getOriginalBlobStartPositionInGridPixels()) {
                    blob.setPositionInScaleUnits(viewport.getScale().toScaleUnits(Math.round(e.getMousePositionInGridPixels())));
                    blob.setSizeInGridPixels(Math.round(e.getOriginalBlobStartPositionInGridPixels() - e.getMousePositionInGridPixels()));
                } else {
                    blob.setPositionInScaleUnits(viewport.getScale().toScaleUnits(e.getOriginalBlobStartPositionInGridPixels()));
                    blob.setSizeInGridPixels(Math.round(e.getOriginalBlobLengthInGridPixels() + e.getMousePositionDeltaInGridPixels()));
                }
                break;
            case START:
                if (e.getMousePositionInGridPixels() > e.getOriginalBlobEndPositionInGridPixels()) {
                    blob.setPositionInScaleUnits(viewport.getScale().toScaleUnits(e.getOriginalBlobEndPositionInGridPixels()));
                    blob.setSizeInGridPixels(Math.round(e.getMousePositionInGridPixels() - e.getOriginalBlobEndPositionInGridPixels()));
                } else {
                    blob.setPositionInScaleUnits(viewport.getScale().toScaleUnits(Math.round(e.getOriginalBlobStartPositionInGridPixels() + e.getMousePositionDeltaInGridPixels())));
                    blob.setSizeInGridPixels(Math.round(e.getOriginalBlobLengthInGridPixels() - e.getMousePositionDeltaInGridPixels()));
                }
                break;
            default:
                throw new IllegalStateException("No case to handle resizingFrom (" + e.getResizingFrom() + ") in ShiftBlobView.onResize");

        }
        updateView();
    }

    private void updateView() {
        blob.getTwin().ifPresent(blobViews::remove);
        blob.setTwin(blob.getUpdatedTwin().orElse(null));
        blob.getTwin().ifPresent(blobViews::add);
        if (blob.getEndPositionInGridPixels() < 0 || blob.getPositionInGridPixels() > viewport.getScale().getEndInGridPixels()) {
            blob.getTwin().ifPresent(twin -> twin.setTwin(null));
            blobViews.remove(blob);
        }
        refresh();
        refreshTwinIfAny();
    }

}
