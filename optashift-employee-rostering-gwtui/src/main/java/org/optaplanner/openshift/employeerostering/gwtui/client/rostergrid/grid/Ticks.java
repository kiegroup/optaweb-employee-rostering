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

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import elemental2.dom.HTMLDivElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;

public class Ticks<T> {

    private final LinearScale<T> scale;
    private final Long stepSize;
    private final Supplier<HTMLDivElement> divFactory;

    Ticks(final LinearScale<T> scale,
          final Long stepSize,
          final Supplier<HTMLDivElement> divFactory) {

        this.scale = scale;
        this.stepSize = stepSize;
        this.divFactory = divFactory;
    }

    public void drawAt(final IsElement target,
                       final Viewport<T> viewport,
                       final Function<T, String> tickText,
                       final Function<T, List<String>> classesToAdd) {

        target.getElement().innerHTML = "";

        for (Long i = 0L; i < scale.getEndInGridPixels(); i += stepSize) {
            final HTMLDivElement tick = divFactory.get();
            tick.textContent = tickText.apply(scale.toScaleUnits(i));
            classesToAdd.apply(scale.toScaleUnits(i)).forEach((clazz) -> tick.classList.add(clazz));
            viewport.setPositionInScreenPixels(() -> tick, i, -2L);
            viewport.setSizeInScreenPixels(() -> tick, stepSize, -2L);
            target.getElement().appendChild(tick);
        }
    }
}
