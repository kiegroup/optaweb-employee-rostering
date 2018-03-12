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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.employeeroster;

import java.time.OffsetDateTime;

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
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;

@Templated
public class EmployeeAvailabilityBlobView implements BlobView<OffsetDateTime, EmployeeAvailabilityBlob> {

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
    private EmployeeRosterPage page;

    private Viewport<OffsetDateTime> viewport;
    private ListView<EmployeeAvailabilityBlob> blobViews;
    private Runnable onDestroy;

    private EmployeeAvailabilityBlob blob;

    @Override
    public ListElementView<EmployeeAvailabilityBlob> setup(final EmployeeAvailabilityBlob blob,
                                                           final ListView<EmployeeAvailabilityBlob> blobViews) {

        this.blobViews = blobViews;
        this.blob = blob;

        refresh();

        // FIXME: Enable draggability and resizability after backend supports it.

        return this;
    }

    public void refresh() {
        setClassProperty("desired", blob.getAvailability().getState() == EmployeeAvailabilityState.DESIRED);
        setClassProperty("undesired", blob.getAvailability().getState() == EmployeeAvailabilityState.UNDESIRED);
        setClassProperty("unavailable", blob.getAvailability().getState() == EmployeeAvailabilityState.UNAVAILABLE);

        RosterState rosterState = page.getCurrentEmployeeRosterView().getRosterState();
        setClassProperty("historic", rosterState.isHistoric(blob.getAvailability().getStartTime().atDate(blob.getAvailability().getDate())));
        setClassProperty("published", rosterState.isPublished(blob.getAvailability().getStartTime().atDate(blob.getAvailability().getDate())));
        setClassProperty("draft", rosterState.isDraft(blob.getAvailability().getStartTime().atDate(blob.getAvailability().getDate())));

        viewport.setPositionInScreenPixels(this, blob.getPositionInGridPixels(), BLOB_POSITION_DISPLACEMENT_IN_SCREEN_PIXELS);
        viewport.setSizeInScreenPixels(this, blob.getSizeInGridPixels(), BLOB_SIZE_DISPLACEMENT_IN_SCREEN_PIXELS);

        updateLabel();
    }

    private void setClassProperty(String clazz, boolean isSet) {
        if (isSet) {
            getElement().classList.add(clazz);
        } else {
            getElement().classList.remove(clazz);
        }
    }

    private boolean onResize(final Long newSizeInGridPixels) {
        blob.setSizeInGridPixels(newSizeInGridPixels);
        //TODO: Update Availability start and end times
        //ShiftRestServiceBuilder.updateShift(blob.getShift().getTenantId(), new ShiftView(blob.getShift()));

        updateLabel();
        return true;
    }

    private boolean onDrag(final Long newPositionInGridPixels) {
        blob.setPositionInScaleUnits(viewport.getScale().toScaleUnits(newPositionInGridPixels));
        //TODO: Update Availability start and end times
        //ShiftRestServiceBuilder.updateShift(blob.getShift().getTenantId(), new ShiftView(blob.getShift()));

        updateLabel();
        return true;
    }

    @EventHandler("blob")
    public void onBlobClicked(final @ForEvent("click") MouseEvent e) {
        page.getBlobPopover().showFor(this);
    }

    private void updateLabel() {
        label.textContent = blob.getLabel();
    }

    @Override
    public BlobView<OffsetDateTime, EmployeeAvailabilityBlob> withViewport(final Viewport<OffsetDateTime> viewport) {
        this.viewport = viewport;
        return this;
    }

    @Override
    public BlobView<OffsetDateTime, EmployeeAvailabilityBlob> withSubLane(final SubLane<OffsetDateTime> subLaneView) {
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

    public void remove() {
        blobViews.remove(blob);
    }

    @Override
    public Blob<OffsetDateTime> getBlob() {
        return blob;
    }
}
