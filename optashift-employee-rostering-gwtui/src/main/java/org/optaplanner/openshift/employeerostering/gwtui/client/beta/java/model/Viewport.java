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

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model;

import java.util.List;

import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.BlobView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Orientation.VERTICAL;

public abstract class Viewport<T> {

    public final Long gridPixelSizeInScreenPixels;
    public final Orientation orientation;
    public final List<Lane<T>> lanes;
    public final FiniteLinearScale<T> scale;

    protected Viewport(final Long gridPixelSizeInScreenPixels,
                       final Orientation orientation,
                       final List<Lane<T>> lanes,
                       final FiniteLinearScale<T> scale) {

        this.gridPixelSizeInScreenPixels = gridPixelSizeInScreenPixels;
        this.orientation = orientation;
        this.lanes = lanes;
        this.scale = scale;
    }

    public abstract void drawGridLinesAt(final IsElement container);

    public abstract Blob<T> newBlob(final Lane<T> lane, final T positionInScaleUnits);

    public abstract BlobView<T, ?> newBlobView();

    public <Y> Y decideBasedOnOrientation(final Y verticalOption, final Y horizontalOption) {
        return orientation.equals(VERTICAL) ? verticalOption : horizontalOption;
    }

    public Long getSizeInGridPixels(final IsElement element) {
        return toGridPixels(orientation.getSize(element));
    }

    public Long toGridPixels(final Long screenPixels) {
        return screenPixels / gridPixelSizeInScreenPixels;
    }

    public Long toScreenPixels(final Long gridPixels) {
        return gridPixels * gridPixelSizeInScreenPixels;
    }

    public void setSizeInScreenPixels(final IsElement element, final Long sizeInGridPixels, final Long offsetInScreenPixels) {
        orientation.scale(element, sizeInGridPixels, this, offsetInScreenPixels);
    }

    public void setPositionInScreenPixels(final IsElement element, final Long positionInGridPixels, final Long offsetInScreenPixels) {
        orientation.position(element, positionInGridPixels, this, offsetInScreenPixels);
    }

    public Long getSizeInGridPixels() {
        return scale.toGridPixels(scale.getEnd());
    }

    public List<Lane<T>> getLanes() {
        return lanes;
    }
}
