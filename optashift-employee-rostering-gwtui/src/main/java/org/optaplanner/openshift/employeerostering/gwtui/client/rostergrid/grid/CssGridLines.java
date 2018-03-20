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

package org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;

public class CssGridLines {

    private final Long softLineStepInGridPixels;
    private final Long strongLineStepInGridPixels;
    private final Supplier<HTMLElement> divFactory;

    private final List<HTMLElement> gridLineElements;

    CssGridLines(final Long softStep,
                 final Long harshStep,
                 final Supplier<HTMLElement> divFactory) {

        softLineStepInGridPixels = softStep;
        strongLineStepInGridPixels = harshStep;
        this.divFactory = divFactory;
        gridLineElements = new ArrayList<>();
    }

    public void drawAt(final IsElement target, final Viewport<?> viewport) {

        final HTMLElement targetElement = target.getElement();

        gridLineElements.forEach((e) -> e.remove());
        gridLineElements.clear();

        for (long i = 0; i < viewport.getScale().getEndInGridPixels(); i += softLineStepInGridPixels) {
            HTMLElement gridLine = divFactory.get();

            // Assumes strongLineStepInGridPixels is a multiple of softLineStepInGridPixels
            if (i % strongLineStepInGridPixels == 0) {
                gridLine.classList.add("strong-grid-line");
            } else {
                gridLine.classList.add("soft-grid-line");
            }
            viewport.setPositionInScreenPixels(() -> gridLine, i, 0L);
            viewport.setSizeInScreenPixels(() -> gridLine, 1L, 0L);
            viewport.setGroupPosition(() -> gridLine, 0L);
            viewport.setGroupSizeInScreenPixels(() -> gridLine, viewport.getGroupEndPosition() + 1, 0L);
            targetElement.appendChild(gridLine);
            gridLineElements.add(gridLine);
        }
    }
}
