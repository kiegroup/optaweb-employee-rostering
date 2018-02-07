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

public interface Blob<T> {

    T getPosition();

    void setPosition(final T position);

    Long getSizeInGridPixels();

    void setSizeInGridPixels(final Long sizeInGridPixels);

    default T getEndPosition(final LinearScale<T> scale) {
        return scale.from(getEndPositionInGridPixels(scale));
    }

    default Long getEndPositionInGridPixels(final LinearScale<T> scale) {
        return scale.to(getPosition()) + getSizeInGridPixels();
    }

    default boolean collidesWith(final Blob<T> other, final LinearScale<T> scale) {

        final Long x0 = scale.to(getPosition());
        final Long x1 = getEndPositionInGridPixels(scale);
        final Long y0 = scale.to(other.getPosition());
        final Long y1 = other.getEndPositionInGridPixels(scale);

        final Long intersectionLeft = y1 - x0;
        final Long intersectionRight = x1 - y0;

        return intersectionLeft > 0 && intersectionLeft <= getSizeInGridPixels() ||
                intersectionRight > 0 && intersectionRight <= other.getSizeInGridPixels();
    }
}
