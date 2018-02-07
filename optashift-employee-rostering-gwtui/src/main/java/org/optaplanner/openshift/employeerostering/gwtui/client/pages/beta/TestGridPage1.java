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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.DefaultGridLines;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.test.TestBlob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.test.TestBlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.BlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.ViewportView;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport.Orientation.VERTICAL;

@Templated("TestGridPage.html")
public class TestGridPage1 implements Page {

    @Inject
    @DataField("viewport")
    private ViewportView<Long> viewportView;

    @Inject
    private TestLanes testLanes;

    @Inject
    private DefaultGridLines gridLines;

    @Inject
    private ManagedInstance<TestBlobView> blobViews;

    @PostConstruct
    public void init() {
        viewportView.setViewport(new Viewport<Long>() {

            {
                orientation = VERTICAL;
                sizeInGridPixels = 84L;
                gridPixelSizeInScreenPixels = 12L;
                defaultNewBlobSizeInGridPixels = 4L;
                scale = new LinearScale<Long>() {

                    @Override
                    public Long to(final Long value) {
                        return value;
                    }

                    @Override
                    public Long from(final Long value) {
                        return value;
                    }
                };
                lanes = testLanes.getAll();
            }

            @Override
            public void drawGridLinesAt(final IsElement container) {
                gridLines.draw(container, this);
            }

            @Override
            public Blob<Long> newBlob(final Long positionInGridPixels) {
                return new TestBlob("New", defaultNewBlobSizeInGridPixels, positionInGridPixels);
            }

            @Override
            public BlobView<Long, ?> newBlobView() {
                return blobViews.get();
            }
        });
    }
}
