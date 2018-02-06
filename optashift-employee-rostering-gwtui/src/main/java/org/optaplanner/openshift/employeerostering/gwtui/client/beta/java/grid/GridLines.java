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

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.VerticalGridLine.Strength.STRONG;
import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.VerticalGridLine.Strength.WEAK;

@Dependent
public class GridLines {

    @Inject
    private ManagedInstance<VerticalGridLine> gridLines;

    private final Integer weakLineInterval = 2;
    private final Integer strongLineInterval = 12;

    private final Integer offset = 6; // in screen pixels;

    public void draw(final Viewport viewport,
                     final HTMLElement container) {

        for (int i = 1; i < viewport.sizeInPixels; i++) {

            if (isStrong(i)) {
                addTo(container, newGridLine(STRONG, viewport, i));
                continue;
            }

            if (isWeak(i)) {
                addTo(container, newGridLine(WEAK, viewport, i));
                continue;
            }
        }
    }

    private boolean isWeak(final int i) {
        return i % weakLineInterval == 0;
    }

    private boolean isStrong(final int i) {
        return i % strongLineInterval == 0;
    }

    private void addTo(final HTMLElement container,
                       final VerticalGridLine gridLine) {

        container.appendChild(gridLine.getElement());
    }

    private VerticalGridLine newGridLine(final VerticalGridLine.Strength strength,
                                         final Viewport viewport,
                                         final int currentPixel) {
        return gridLines.get()
                .withStrength(strength)
                .withPosition(currentPixel * viewport.pixelSize + offset);
    }
}
