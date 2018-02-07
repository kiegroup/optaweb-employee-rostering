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

import elemental2.dom.CSSProperties.HeightUnionType;
import elemental2.dom.CSSProperties.WidthUnionType;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.BlobView;

public abstract class Viewport {

    public List<Lane> lanes;

    public Integer sizeInPixels;

    public Integer pixelSize;

    public Integer defaultNewBlobSizeInPixels;

    public Orientation orientation;

    public LinearScale scale;

    public abstract void drawGridLinesAt(final IsElement container);

    public abstract Blob newBlob(final Integer position);

    public abstract BlobView<?> newBlobView();

    public Integer getSize(final IsElement element) {
        return orientation.getSize(element) / pixelSize;
    }

    public Integer scale(final Integer value) {
        return value * pixelSize;
    }

    public void scale(final IsElement element, final Integer value, final Integer offset) {
        orientation.scale(element, value, this, offset);
    }

    public void position(final IsElement element, final Integer value, final Integer offset) {
        orientation.position(element, value, this, offset);
    }

    public enum Orientation {
        VERTICAL {
            @Override
            void position(final IsElement element, final Integer position, final Viewport viewport, Integer offset) {
                element.getElement().style.top = viewport.scale(position) + offset + "px";
            }

            @Override
            void scale(final IsElement element, final Integer size, final Viewport viewport, final Integer offset) {
                element.getElement().style.height = HeightUnionType.of(viewport.scale(size) + offset + "px");
            }

            @Override
            public Integer getSize(final IsElement element) {
                final String size = element.getElement().style.height.asString();
                return size.isEmpty() ? 0 : Integer.parseInt(size.substring(0, size.length() - 2));
            }
        },
        HORIZONTAL {
            @Override
            void position(final IsElement element, final Integer position, final Viewport viewport, final Integer offset) {
                element.getElement().style.left = viewport.scale(position) + offset + "px";
            }

            @Override
            void scale(final IsElement element, final Integer size, final Viewport viewport, final Integer offset) {
                element.getElement().style.width = WidthUnionType.of(viewport.scale(size) + offset + "px");
            }

            @Override
            public Integer getSize(final IsElement element) {
                final String size = element.getElement().style.width.asString();
                return size.isEmpty() ? 0 : Integer.parseInt(size.substring(0, size.length() - 2));
            }
        };

        abstract void position(final IsElement element, final Integer position, final Viewport viewport, final Integer offset);

        abstract void scale(final IsElement element, final Integer size, final Viewport viewport, final Integer offset);

        public abstract Integer getSize(final IsElement element);
    }
}
