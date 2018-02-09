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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.DefaultGridLines;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.FiniteLinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Orientation;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.test.TestBlob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.test.TestBlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.BlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.ViewportView;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Orientation.VERTICAL;

@Templated("TestGridPage.html")
public class TestGridPage1 implements Page {

    @Inject
    @DataField("viewport")
    private ViewportView<Long> viewportView;

    @Inject
    private TestLanes testLanes;

    @Inject
    private ManagedInstance<TestBlobView> blobViews;

    @PostConstruct
    public void init() {
        viewportView.setViewport(new Viewport<Long>() {

            private final List<Lane<Long>> lanes = testLanes.getAll();

            @Override
            public void drawGridLinesAt(final IsElement container) {
                new DefaultGridLines().draw(container, this);
            }

            @Override
            public Lane<Long> newLane() {
                final List<SubLane<Long>> subLanes = new ArrayList<>();
                final SubLane<Long> subLane = new SubLane<>(new ArrayList<>());
                subLanes.add(subLane);
                return new Lane<>("New", subLanes);
            }

            @Override
            public Blob<Long> newBlob(final Lane<Long> lane, final Long positionInScaleUnits) {
                return new TestBlob("New", 4L, positionInScaleUnits);
            }

            @Override
            public BlobView<Long, ?> newBlobView() {
                return blobViews.get();
            }

            @Override
            public List<Lane<Long>> getLanes() {
                return lanes;
            }

            @Override
            public Long getGridPixelSizeInScreenPixels() {
                return 12L;
            }

            @Override
            public Orientation getOrientation() {
                return VERTICAL;
            }

            @Override
            public FiniteLinearScale<Long> getScale() {
                return new Finite1to1LinearScaleFrom0To(1080L);
            }
        });
    }
}
