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

import elemental2.dom.CSSProperties;
import org.jboss.errai.common.client.api.elemental2.IsElement;

public enum Orientation {
    VERTICAL {
        @Override
        void position(final IsElement element, final Long positionInGridPixels, final Viewport viewport, Long offsetInScreenPixels) {
            element.getElement().style.top = viewport.toScreenPixels(positionInGridPixels) + offsetInScreenPixels + "px";
        }

        @Override
        void scale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport, final Long offsetInScreenPixels) {
            element.getElement().style.height = CSSProperties.HeightUnionType.of(viewport.toScreenPixels(sizeInGridPixels) + offsetInScreenPixels + "px");
        }

        @Override
        public Long getSize(final IsElement element) {
            final String size = element.getElement().style.height.asString();
            return size.isEmpty() ? 0 : Long.parseLong(size.substring(0, size.length() - 2));
        }
    },
    HORIZONTAL {
        @Override
        void position(final IsElement element, final Long positionInGridPixels, final Viewport viewport, final Long offsetInScreenPixels) {
            element.getElement().style.left = viewport.toScreenPixels(positionInGridPixels) + offsetInScreenPixels + "px";
        }

        @Override
        void scale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport, final Long offsetInScreenPixels) {
            element.getElement().style.width = CSSProperties.WidthUnionType.of(viewport.toScreenPixels(sizeInGridPixels) + offsetInScreenPixels + "px");
        }

        @Override
        public Long getSize(final IsElement element) {
            final String size = element.getElement().style.width.asString();
            return size.isEmpty() ? 0 : Long.parseLong(size.substring(0, size.length() - 2));
        }
    };

    abstract void position(final IsElement element, final Long positionInGridPixels, final Viewport viewport, final Long offsetInScreenPixels);

    abstract void scale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport, final Long offsetInScreenPixels);

    public abstract Long getSize(final IsElement element);
}
