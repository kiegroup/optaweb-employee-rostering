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

import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;

public class CssGridLines {

    private final Long softLineStepInGridPixels;
    private final Long strongLineStepInGridPixels;

    CssGridLines(final Long softStep,
                 final Long harshStep) {

        softLineStepInGridPixels = softStep;
        strongLineStepInGridPixels = harshStep;
    }

    public void drawAt(final IsElement target, final Viewport<?> viewport) {

        final HTMLElement targetElement = target.getElement();

        targetElement.style.backgroundPosition = "5px 5px, 5px 5px";

        targetElement.style.backgroundImage =
                "linear-gradient(" + getRotation(viewport) + "deg, rgba(0, 0, 0, 0.1) 1px, transparent 1px)," +
                        " linear-gradient(" + getRotation(viewport) + "deg, rgba(0, 0, 0, 0.2) 1px, transparent 1px)";

        targetElement.style.backgroundSize =
                getSoftLinesInterval(viewport) + getSoftLinesInterval(viewport) + ", " +
                        getHarshLinesInterval(viewport) + getHarshLinesInterval(viewport);
    }

    private String getHarshLinesInterval(final Viewport<?> viewport) {
        return viewport.toScreenPixels(strongLineStepInGridPixels) + "px ";
    }

    private String getSoftLinesInterval(final Viewport<?> viewport) {
        return viewport.toScreenPixels(softLineStepInGridPixels) + "px ";
    }

    private Integer getRotation(final Viewport<?> viewport) {
        return viewport.decideBasedOnOrientation(0, 90);
    }
}
