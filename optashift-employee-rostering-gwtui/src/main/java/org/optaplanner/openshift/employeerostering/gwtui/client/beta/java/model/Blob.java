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

public interface Blob {

    String getLabel();

    Integer getSize();

    Integer getPosition();

    default boolean collidesWith(final Blob other) {

        final int x0 = getPosition();
        final int x1 = getPosition() + getSize();
        final int y0 = other.getPosition();
        final int y1 = other.getPosition() + other.getSize();

        final int intersectionLeft = y1 - x0;
        final int intersectionRight = x1 - y0;

        return intersectionLeft > 0 && intersectionLeft <= getSize() ||
                intersectionRight > 0 && intersectionRight <= other.getSize();
    }

    void setLabel(String label);

    void setPosition(Integer position);

    void setSize(Integer size);
}
