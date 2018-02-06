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

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.test;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.BlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.list.ListElementView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.list.ListView;

import static elemental2.dom.CSSProperties.HeightUnionType;

@Templated
public class TestBlobView implements BlobView<TestBlob> {

    @Inject
    @Named("span")
    @DataField("label")
    private HTMLElement label;

    @Inject
    @DataField("close")
    private HTMLDivElement close;

    private TestBlob blob;
    private ListView<TestBlob, ?> list;
    private Viewport viewport;

    @Override
    public ListElementView<TestBlob> setup(final TestBlob blob,
                                           final ListView<TestBlob, ?> list) {

        this.blob = blob;
        this.list = list;

        label.textContent = blob.getLabel();
        getElement().style.top = blob.getPosition() * viewport.pixelSize + "px";
        getElement().style.height = HeightUnionType.of(blob.getSize() * viewport.pixelSize + "px");
        return this;
    }

    @EventHandler("close")
    public void onCloseClicked(final ClickEvent e) {
        list.remove(blob);
    }

    @Override
    public TestBlobView withViewport(final Viewport viewport) {
        this.viewport = viewport;
        return this;
    }
}
