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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.beta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.demo.ShiftBlob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.demo.ShiftBlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.DefaultGridLines;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.PositiveMinutesScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.BlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.ViewportView;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport.Orientation.HORIZONTAL;

@Templated("TestGridPage.html")
public class DomainGridDemo implements Page {

    @Inject
    @DataField("viewport")
    private ViewportView viewportView;

    @Inject
    private DefaultGridLines gridLines;

    @Inject
    private ManagedInstance<ShiftBlobView> blobViews;

    @PostConstruct
    public void init() {

        final LocalDateTime start = LocalDateTime.of(2018, 2, 7, 0, 0);
        final LocalDateTime end = start.plusWeeks(1);

        viewportView.setViewport(new Viewport() {

            {
                orientation = HORIZONTAL;
                gridPixelSizeInScreenPixels = 8;
                sizeInGridPixels = 180;
                defaultNewBlobSizeInGridPixels = 8;
                domainScaleInGridPixels = new PositiveMinutesScale(start, end);
                lanes = new ArrayList<>(Collections.singletonList(new Lane("Lane", new ArrayList<>(Collections.singletonList(new SubLane(new ArrayList<>()))))));
            }

            @Override
            public void drawGridLinesAt(final IsElement target) {
                gridLines.draw(target, this);
            }

            @Override
            public Blob newBlob(final Integer position) {

                return new ShiftBlob(new Shift(
                        1,
                        new Spot(1, "Emergency Room", new HashSet<>()),
                        new TimeSlot(1, start, end)));
            }

            @Override
            public BlobView<?> newBlobView() {
                return blobViews.get();
            }
        });
    }
}
