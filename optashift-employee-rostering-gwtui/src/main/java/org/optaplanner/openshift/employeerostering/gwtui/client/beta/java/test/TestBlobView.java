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
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.BlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.SubLaneView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport.Orientation.VERTICAL;

@Templated
public class TestBlobView implements BlobView<TestBlob> {

    @Inject
    @DataField("blob")
    private HTMLDivElement root;

    @Inject
    @Named("span")
    @DataField("label")
    private HTMLElement label;

    private TestBlob blob;
    private ListView<TestBlob> list;
    private Viewport viewport;
    private SubLaneView subLaneView;

    @Override
    public ListElementView<TestBlob> setup(final TestBlob blob,
                                           final ListView<TestBlob> list) {

        this.blob = blob;
        this.list = list;

        label.textContent = blob.getLabel();
        viewport.scale(this, blob.getSize(), 0);
        viewport.position(this, blob.getPosition(), 0);

        makeDraggable(getElement(),
                      subLaneView.getElement(),
                      viewport.pixelSize,
                      viewport.orientation.equals(VERTICAL) ? "y" : "x");

        makeResizable(getElement(),
                      viewport.pixelSize,
                      viewport.orientation.equals(VERTICAL) ? "s" : "e");

        return this;
    }

    private native void makeResizable(final HTMLElement blob,
                                      final int pixelSize,
                                      final String orientation) /*-{
        var that = this;
        var $blob = $wnd.jQuery(blob);

        $blob.resizable({
            handles: orientation,
            minHeight: 0,
            resize: function (e, ui) {
                if (orientation === 's') {
                    var coordinateY = ui.size.height + 2 * pixelSize;
                    ui.size.height = Math.floor(coordinateY - (coordinateY % pixelSize));
                } else if (orientation === 'e') {
                    var coordinateX = ui.size.width + 2 * pixelSize;
                    ui.size.width = Math.floor(coordinateX - (coordinateX % pixelSize));
                }
                that.@org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.test.TestBlobView::onResize(II)(ui.size.height, ui.size.width);
            }
        });
    }-*/;

    private native void makeDraggable(final HTMLElement blob,
                                      final HTMLElement subLane,
                                      final int pixelSize,
                                      final String orientation) /*-{

        var that = this;
        var $blob = $wnd.jQuery(blob);
        var $subLane = $wnd.jQuery(subLane);

        $blob.draggable({
            addClasses: false,
            cancel: '.blob div',
            containment: $subLane,
            axis: orientation,
            grid: [pixelSize, pixelSize],
            drag: function (e, ui) {
                that.@org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.test.TestBlobView::onDrag(II)(ui.position.top, ui.position.left);
            },
            scroll: true,
            scrollSpeed: 10,
            scrollSensitivity: 50
        });
    }-*/;

    public boolean onResize(final int height, final int width) {
        final Integer newSize = (viewport.orientation.equals(VERTICAL) ? height : width) / viewport.pixelSize;
        final Integer originalSize = blob.getSize();

        if (!newSize.equals(originalSize)) {
            blob.setSize(newSize);
            if (!subLaneView.hasSpaceFor(blob)) {
                DomGlobal.console.info("Collision!"); //TODO: Restrict resizing if a collision occurs.
                blob.setSize(originalSize);
                return false;
            }
        }

        return true;
    }

    public boolean onDrag(final int top, final int left) {

        final Integer newPosition = (viewport.orientation.equals(VERTICAL) ? top : left) / viewport.pixelSize;
        final Integer originalPosition = blob.getPosition();

        if (!newPosition.equals(originalPosition)) {
            blob.setPosition(newPosition);
            if (!subLaneView.hasSpaceFor(blob)) {
                DomGlobal.console.info("Collision!"); //TODO: Restrict dragging if a collision occurs.
                blob.setPosition(originalPosition);
                return false;
            }
        }

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
    public BlobView<TestBlob> withViewport(final Viewport viewport) {
        this.viewport = viewport;
        return this;
    }

    @Override
    public BlobView<TestBlob> withSubLaneView(final SubLaneView subLaneView) {
        this.subLaneView = subLaneView;
        return this;
    }
}
