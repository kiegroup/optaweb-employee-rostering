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
        void position(final IsElement element, final Long positionInGridPixels, final Viewport viewport, Long offsetInScreenPixels) {
            Long position = clamp(positionInGridPixels + viewport.getHeaderColumns(),
                    viewport.getHeaderColumns(), viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns());
            absPosition(element, position, viewport, offsetInScreenPixels);
        }

        @Override
        void absPosition(IsElement element, Long positionInGridPixels, Viewport viewport, Long offsetInScreenPixels) {
            Long size = getSize(element);
            Long endPosition = clamp(positionInGridPixels + size, viewport.getHeaderColumns(), viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns());
            element.getElement().style.set("grid-row-start", (positionInGridPixels + 1) + "");
            element.getElement().style.set("grid-row-end", (endPosition + 1) + "");
        }

        @Override
        void groupPosition(IsElement element, Long positionInGridPixels, Viewport viewport, Long offsetInScreenPixels) {
            Long position = clamp(positionInGridPixels + viewport.getHeaderRows(), viewport.getHeaderRows(), Long.MAX_VALUE);
            absGroupPosition(element, position, viewport, offsetInScreenPixels);
        }

        @Override
        void absGroupPosition(IsElement element, Long positionInGridPixels, Viewport viewport, Long offsetInScreenPixels) {
            Long endPosition = positionInGridPixels + getGroupSize(element);
            element.getElement().style.set("grid-column-start", (positionInGridPixels + 1) + "");
            element.getElement().style.set("grid-column-end", (endPosition + 1) + "");
        }

        @Override
        void scale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport, final Long offsetInScreenPixels) {
            Long startPosition = getStartPosition(element) - 1;
            Long endPosition = clamp(startPosition + sizeInGridPixels, viewport.getHeaderColumns(), viewport.getHeaderColumns() + viewport.getScale().getEndInGridPixels());
            element.getElement().style.set("grid-row-end", (endPosition + 1) + "");
        }

        @Override
        void groupScale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport, final Long offsetInScreenPixels) {
            Long startPosition = getGroupStartPosition(element) - 1;
            element.getElement().style.set("grid-column-end", (startPosition + sizeInGridPixels + 1) + "");
        }

        @Override
        public Long getSize(final IsElement element) {
            return getEndPosition(element) - getStartPosition(element);
        }

        public Long getGroupSize(final IsElement element) {
            return getGroupEndPosition(element) - getGroupStartPosition(element);
        }

        private Long getStartPosition(final IsElement element) {
            final String position = element.getElement().style.get("grid-row-start");
            return position.isEmpty() ? 1 : Long.parseLong(position);
        }

        private Long getEndPosition(final IsElement element) {
            final String position = element.getElement().style.get("grid-row-end");
            return position.isEmpty() ? 1 : Long.parseLong(position);
        }

        private Long getGroupStartPosition(final IsElement element) {
            final String position = element.getElement().style.get("grid-column-start");
            return position.isEmpty() ? 1 : Long.parseLong(position);
        }

        private Long getGroupEndPosition(final IsElement element) {
            final String position = element.getElement().style.get("grid-column-end");
            return position.isEmpty() ? 1 : Long.parseLong(position);
        }

    },
    HORIZONTAL {

        @Override
        void position(final IsElement element, final Long positionInGridPixels, final Viewport viewport, Long offsetInScreenPixels) {
            Long position = clamp(positionInGridPixels + viewport.getHeaderColumns(),
                    viewport.getHeaderColumns(), viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns());
            Long size = getSize(element);
            Long endPosition = clamp(positionInGridPixels + viewport.getHeaderColumns() + size, viewport.getHeaderColumns(), viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns());
            element.getElement().style.set("grid-column-start", (position + 1) + "");
            element.getElement().style.set("--grid-column-start", (positionInGridPixels + viewport.getHeaderColumns()) + "");
            element.getElement().style.set("grid-column-end", (endPosition + 1) + "");
            element.getElement().style.set("--grid-column-end", (positionInGridPixels + size + viewport.getHeaderColumns()) + "");
        }

        @Override
        void absPosition(IsElement element, Long positionInGridPixels, Viewport viewport, Long offsetInScreenPixels) {
            Long size = getSize(element);
            Long endPosition = clamp(positionInGridPixels + size, viewport.getHeaderColumns(), viewport.getScale().getEndInGridPixels() + viewport.getHeaderColumns());
            element.getElement().style.set("grid-column-start", (positionInGridPixels + 1) + "");
            element.getElement().style.set("--grid-column-start", (positionInGridPixels) + "");
            element.getElement().style.set("grid-column-end", (endPosition + 1) + "");
            element.getElement().style.set("--grid-column-end", (positionInGridPixels + size) + "");
        }

        @Override
        void groupPosition(IsElement element, Long positionInGridPixels, Viewport viewport, Long offsetInScreenPixels) {
            Long position = clamp(positionInGridPixels + viewport.getHeaderRows(), viewport.getHeaderRows(), Long.MAX_VALUE);
            Long endPosition = positionInGridPixels + getGroupSize(element) + viewport.getHeaderRows();
            element.getElement().style.set("grid-row-start", (position + 1) + "");
            element.getElement().style.set("--grid-row-start", (positionInGridPixels + viewport.getHeaderRows()) + "");
            element.getElement().style.set("grid-row-end", (endPosition + 1) + "");
            element.getElement().style.set("--grid-row-end", (endPosition) + "");
        }

        @Override
        void absGroupPosition(IsElement element, Long positionInGridPixels, Viewport viewport, Long offsetInScreenPixels) {
            Long endPosition = positionInGridPixels + getGroupSize(element);
            element.getElement().style.set("grid-row-start", (positionInGridPixels + 1) + "");
            element.getElement().style.set("--grid-row-start", (positionInGridPixels) + "");
            element.getElement().style.set("grid-row-end", (endPosition + 1) + "");
            element.getElement().style.set("--grid-row-end", (endPosition) + "");
        }

        @Override
        void scale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport, final Long offsetInScreenPixels) {
            Long startPosition = getStartPosition(element);
            Long endPosition = clamp(startPosition + sizeInGridPixels, viewport.getHeaderColumns(), viewport.getHeaderColumns() + viewport.getScale().getEndInGridPixels());
            element.getElement().style.set("grid-column-end", (endPosition + 1) + "");
            element.getElement().style.set("--grid-column-end", (startPosition + sizeInGridPixels) + "");
        }

        @Override
        void groupScale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport, final Long offsetInScreenPixels) {
            Long startPosition = getGroupStartPosition(element);
            element.getElement().style.set("grid-row-end", (startPosition + sizeInGridPixels + 1) + "");
            element.getElement().style.set("--grid-row-end", (startPosition + sizeInGridPixels) + "");
        }

        @Override
        public Long getSize(final IsElement element) {
            return getEndPosition(element) - getStartPosition(element);
        }

        public Long getGroupSize(final IsElement element) {
            return getGroupEndPosition(element) - getGroupStartPosition(element);
        }

        private Long getStartPosition(final IsElement element) {
            final String position = element.getElement().style.get("--grid-column-start");
            return position == null || position.isEmpty() ? 0 : Long.parseLong(position);
        }

        private Long getEndPosition(final IsElement element) {
            final String position = element.getElement().style.get("--grid-column-end");
            return position == null || position.isEmpty() ? 0 : Long.parseLong(position);
        }

        private Long getGroupStartPosition(final IsElement element) {
            final String position = element.getElement().style.get("--grid-row-start");
            return position == null || position.isEmpty() ? 0 : Long.parseLong(position);
        }

        private Long getGroupEndPosition(final IsElement element) {
            final String position = element.getElement().style.get("--grid-row-end");
            return position == null || position.isEmpty() ? 0 : Long.parseLong(position);
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

    abstract void absGroupPosition(final IsElement element, final Long positionInGridPixels, final Viewport viewport, final Long offsetInScreenPixels);

    abstract void groupPosition(final IsElement element, final Long positionInGridPixels, final Viewport viewport, final Long offsetInScreenPixels);

    // Use this for headers
    abstract void absPosition(final IsElement element, final Long positionInGridPixels, final Viewport viewport, final Long offsetInScreenPixels);

    // Use this for everything else (ensures it doesn't conflict with header rows)
    abstract void position(final IsElement element, final Long positionInGridPixels, final Viewport viewport, final Long offsetInScreenPixels);

    abstract void scale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport, final Long offsetInScreenPixels);

    abstract void groupScale(final IsElement element, final Long sizeInGridPixels, final Viewport viewport, final Long offsetInScreenPixels);

    public abstract Long getSize(final IsElement element);
}
