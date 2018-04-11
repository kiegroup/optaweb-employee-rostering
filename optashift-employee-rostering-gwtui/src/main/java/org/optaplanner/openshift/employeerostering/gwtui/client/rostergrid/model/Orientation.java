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

package org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model;

import org.jboss.errai.common.client.api.elemental2.IsElement;

public enum Orientation {
    VERTICAL {

        @Override
        void position(final IsElement element, final Long positionInGridPixels, final Viewport viewport) {
            Long position = clamp(positionInGridPixels + viewport.getHeaderColumns(),
                    viewport.getHeaderColumns(), viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns());
            Long size = getSize(element);
            Long endPosition = clamp(positionInGridPixels + viewport.getHeaderColumns() + size, viewport.getHeaderColumns(), viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns());
            element.getElement().style.set("grid-row-start", (position + 1) + "");
            element.getElement().setAttribute("data-grid-main-axis-start", (positionInGridPixels + viewport.getHeaderColumns()) + "");
            element.getElement().style.set("grid-row-end", (endPosition + 1) + "");
            element.getElement().setAttribute("data-grid-main-axis-end", (positionInGridPixels + size + viewport.getHeaderColumns()) + "");
        }

        @Override
        void absPosition(IsElement element, Long positionInGridPixels, Viewport viewport) {
            Long size = getSize(element);
            Long endPosition = clamp(positionInGridPixels + size, viewport.getHeaderColumns(), viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns());
            element.getElement().style.set("grid-row-start", (positionInGridPixels + 1) + "");
            element.getElement().setAttribute("data-grid-main-axis-start", (positionInGridPixels) + "");
            element.getElement().style.set("grid-row-end", (endPosition + 1) + "");
            element.getElement().setAttribute("data-grid-main-axis-end", (positionInGridPixels + size) + "");
        }

        @Override
        void groupPosition(IsElement element, Long positionInGridPixels, Viewport viewport) {
            Long position = clamp(positionInGridPixels + viewport.getHeaderRows(), viewport.getHeaderRows(), Long.MAX_VALUE);
            Long endPosition = positionInGridPixels + getGroupSize(element) + viewport.getHeaderRows();
            element.getElement().style.set("grid-column-start", (position + 1) + "");
            element.getElement().setAttribute("data-grid-secondary-axis-start", (positionInGridPixels + viewport.getHeaderRows()) + "");
            element.getElement().style.set("grid-column-end", (endPosition + 1) + "");
            element.getElement().setAttribute("data-grid-secondary-axis-end", (endPosition) + "");
        }

        @Override
        void absGroupPosition(IsElement element, Long positionInGridPixels, Viewport viewport) {
            Long endPosition = positionInGridPixels + getGroupSize(element);
            element.getElement().style.set("grid-column-start", (positionInGridPixels + 1) + "");
            element.getElement().setAttribute("data-grid-secondary-axis-start", (positionInGridPixels) + "");
            element.getElement().style.set("grid-column-end", (endPosition + 1) + "");
            element.getElement().setAttribute("data-grid-secondary-axis-end", (endPosition) + "");
        }

        @Override
        void scale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport) {
            Long startPosition = getStartPosition(element);
            Long endPosition = clamp(startPosition + sizeInGridPixels, viewport.getHeaderColumns(), viewport.getHeaderColumns() + viewport.getScale().getEndInGridPixels());
            element.getElement().style.set("grid-row-end", (endPosition + 1) + "");
            element.getElement().setAttribute("data-grid-main-axis-end", (startPosition + sizeInGridPixels) + "");
        }

        @Override
        void groupScale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport) {
            Long startPosition = getGroupStartPosition(element);
            element.getElement().style.set("grid-column-end", (startPosition + sizeInGridPixels + 1) + "");
            element.getElement().setAttribute("data-grid-secondary-axis-end", (startPosition + sizeInGridPixels) + "");
        }

    },
    HORIZONTAL {

        @Override
        void position(final IsElement element, final Long positionInGridPixels, final Viewport viewport) {
            Long position = clamp(positionInGridPixels + viewport.getHeaderColumns(),
                    viewport.getHeaderColumns(), viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns());
            Long size = getSize(element);
            Long endPosition = clamp(positionInGridPixels + viewport.getHeaderColumns() + size, viewport.getHeaderColumns(), viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns());
            element.getElement().style.set("grid-column-start", (position + 1) + "");
            element.getElement().setAttribute("data-grid-main-axis-start", (positionInGridPixels + viewport.getHeaderColumns()) + "");
            element.getElement().style.set("grid-column-end", (endPosition + 1) + "");
            element.getElement().setAttribute("data-grid-main-axis-end", (positionInGridPixels + size + viewport.getHeaderColumns()) + "");
        }

        @Override
        void absPosition(IsElement element, Long positionInGridPixels, Viewport viewport) {
            Long size = getSize(element);
            Long endPosition = clamp(positionInGridPixels + size, viewport.getHeaderColumns(), viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns());
            element.getElement().style.set("grid-column-start", (positionInGridPixels + 1) + "");
            element.getElement().setAttribute("data-grid-main-axis-start", (positionInGridPixels) + "");
            element.getElement().style.set("grid-column-end", (endPosition + 1) + "");
            element.getElement().setAttribute("data-grid-main-axis-end", (positionInGridPixels + size) + "");
        }

        @Override
        void groupPosition(IsElement element, Long positionInGridPixels, Viewport viewport) {
            Long position = clamp(positionInGridPixels + viewport.getHeaderRows(), viewport.getHeaderRows(), Long.MAX_VALUE);
            Long endPosition = positionInGridPixels + getGroupSize(element) + viewport.getHeaderRows();
            element.getElement().style.set("grid-row-start", (position + 1) + "");
            element.getElement().setAttribute("data-grid-secondary-axis-start", (positionInGridPixels + viewport.getHeaderRows()) + "");
            element.getElement().style.set("grid-row-end", (endPosition + 1) + "");
            element.getElement().setAttribute("data-grid-secondary-axis-end", (endPosition) + "");
        }

        @Override
        void absGroupPosition(IsElement element, Long positionInGridPixels, Viewport viewport) {
            Long endPosition = positionInGridPixels + getGroupSize(element);
            element.getElement().style.set("grid-row-start", (positionInGridPixels + 1) + "");
            element.getElement().setAttribute("data-grid-secondary-axis-start", (positionInGridPixels) + "");
            element.getElement().style.set("grid-row-end", (endPosition + 1) + "");
            element.getElement().setAttribute("data-grid-secondary-axis-end", (endPosition) + "");
        }

        @Override
        void scale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport) {
            Long startPosition = getStartPosition(element);
            Long endPosition = clamp(startPosition + sizeInGridPixels, viewport.getHeaderColumns(), viewport.getHeaderColumns() + viewport.getScale().getEndInGridPixels());
            element.getElement().style.set("grid-column-end", (endPosition + 1) + "");
            element.getElement().setAttribute("data-grid-main-axis-end", (startPosition + sizeInGridPixels) + "");
        }

        @Override
        void groupScale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport) {
            Long startPosition = getGroupStartPosition(element);
            element.getElement().style.set("grid-row-end", (startPosition + sizeInGridPixels + 1) + "");
            element.getElement().setAttribute("data-grid-secondary-axis-end", (startPosition + sizeInGridPixels) + "");
        }

    };

    private static Long clamp(Long value, Long min, Long max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        }
        return value;
    }

    /**
     * Positions element relative to the corner of the viewport frame across the secondary axis. Use for headers.
     * 
     * @param element Element to position
     * @param positionInGridPixels New position of element
     * @param viewport Viewport the element is in
     */
    abstract void absGroupPosition(final IsElement element, final Long positionInGridPixels, final Viewport viewport);

    /**
     * Positions element relative to the corner of the header frame across the secondary axis. Use for content.
     * 
     * @param element Element to position
     * @param positionInGridPixels New position of element
     * @param viewport Viewport the element is in
     */
    abstract void groupPosition(final IsElement element, final Long positionInGridPixels, final Viewport viewport);

    /**
     * Positions element relative to the corner of the viewport frame across the main axis. Use for headers.
     * 
     * @param element Element to position
     * @param positionInGridPixels New position of element
     * @param viewport Viewport the element is in
     */
    abstract void absPosition(final IsElement element, final Long positionInGridPixels, final Viewport viewport);

    /**
     * Positions element relative to the corner of the header across the main axis. Use for content.
     * 
     * @param element Element to position
     * @param positionInGridPixels New position of element
     * @param viewport Viewport the element is in
     */
    abstract void position(final IsElement element, final Long positionInGridPixels, final Viewport viewport);

    /**
     * Sets the size across the main axis of the element.
     * 
     * @param element Element to set the size of
     * @param sizeInGridPixels New size of element
     * @param viewport Viewport the element is in
     */
    abstract void scale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport);

    /**
     * Sets the size across the secondary axis of the element.
     * 
     * @param element Element to set the size of
     * @param sizeInGridPixels New size of element
     * @param viewport Viewport the element is in
     */
    abstract void groupScale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport);

    /**
     * Get the size across the main axis of the element
     * 
     * @param element Element to get the size of.
     * @return The size across the main axis for element, in grid pixels.
     */
    public Long getSize(final IsElement element) {
        return getEndPosition(element) - getStartPosition(element);
    }

    public Long getGroupSize(final IsElement element) {
        return getGroupEndPosition(element) - getGroupStartPosition(element);
    }

    public Long getStartPosition(final IsElement element) {
        final String position = element.getElement().getAttribute("data-grid-main-axis-start");
        return position == null || position.isEmpty() ? 0 : Long.parseLong(position);
    }

    public Long getEndPosition(final IsElement element) {
        final String position = element.getElement().getAttribute("data-grid-main-axis-end");
        return position == null || position.isEmpty() ? 0 : Long.parseLong(position);
    }

    public Long getGroupStartPosition(final IsElement element) {
        final String position = element.getElement().getAttribute("data-grid-secondary-axis-start");
        return position == null || position.isEmpty() ? 0 : Long.parseLong(position);
    }

    public Long getGroupEndPosition(final IsElement element) {
        final String position = element.getElement().getAttribute("data-grid-secondary-axis-end");
        return position == null || position.isEmpty() ? 0 : Long.parseLong(position);
    }
}
