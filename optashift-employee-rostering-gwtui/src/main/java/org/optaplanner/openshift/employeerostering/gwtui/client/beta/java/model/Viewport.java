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

    public abstract void drawGridLinesAt(final IsElement target);

    public abstract void drawTicksAt(final IsElement target);

    public abstract Lane<T> newLane();

    public abstract Blob<T> newBlob(final Lane<T> lane, final T positionInScaleUnits);

    public abstract BlobView<T, ?> newBlobView();

    public abstract List<Lane<T>> getLanes();

    public abstract Long getGridPixelSizeInScreenPixels();

    public abstract Orientation getOrientation();

    public abstract FiniteLinearScale<T> getScale();

    public <Y> Y decideBasedOnOrientation(final Y verticalOption, final Y horizontalOption) {
        return getOrientation().equals(VERTICAL) ? verticalOption : horizontalOption;
    }

    public Long getSizeInGridPixels(final IsElement element) {
        return toGridPixels(getOrientation().getSize(element));
    }

    public Long toGridPixels(final Long screenPixels) {
        return screenPixels / getGridPixelSizeInScreenPixels();
    }

    public Long toScreenPixels(final Long gridPixels) {
        return gridPixels * getGridPixelSizeInScreenPixels();
    }

    public void setSizeInScreenPixels(final IsElement element, final Long sizeInGridPixels, final Long offsetInScreenPixels) {
        getOrientation().scale(element, sizeInGridPixels, this, offsetInScreenPixels);
    }

    public void setPositionInScreenPixels(final IsElement element, final Long positionInGridPixels, final Long offsetInScreenPixels) {
        getOrientation().position(element, positionInGridPixels, this, offsetInScreenPixels);
    }

    public Long getSizeInGridPixels() {
        return getScale().toGridPixels(getScale().getEnd());
    }
}
