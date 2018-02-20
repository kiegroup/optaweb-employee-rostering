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

import java.util.function.Function;
import java.util.function.Supplier;

import elemental2.dom.HTMLDivElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;

public class Ticks<T> {

    private final LinearScale<T> scale;
    private final Long softStepSize;
    private final Long harshStepSize;
    private final Supplier<HTMLDivElement> divFactory;

    Ticks(final LinearScale<T> scale,
          final Long softStepSize,
          final Long harshStepSize,
          final Supplier<HTMLDivElement> divFactory) {

        this.scale = scale;
        this.softStepSize = softStepSize;
        this.harshStepSize = harshStepSize;
        this.divFactory = divFactory;
    }

    public void drawAt(final IsElement target,
                       final Viewport<T> viewport,
                       final Function<T, String> tickText) {

        target.getElement().innerHTML = "";

        for (Long i = 0L; i <= scale.getEndInGridPixels(); i += softStepSize) {

            final HTMLDivElement tick = divFactory.get();
            if (i % harshStepSize == 0) {
                tick.classList.add("harsh");
            }
            tick.textContent = tickText.apply(scale.toScaleUnits(i));
            viewport.setPositionInScreenPixels(() -> tick, i, -2L);
            target.getElement().appendChild(tick);
        }
    }
}
