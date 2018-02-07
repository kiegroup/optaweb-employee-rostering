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

import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport.Orientation.VERTICAL;

@Dependent
public class DefaultGridLines {

    private static final Integer WEAK_LINE_INTERVAL = 2;
    private static final Integer STRONG_LINE_INTERVAL = 12;

    public void draw(final IsElement container, final Viewport viewport) {

        HTMLElement element = container.getElement();

        element.style.backgroundImage =
                "linear-gradient(" + rotation(viewport) + "rgba(0, 0, 0, 0.1) 1px, transparent 1px)," +
                        " linear-gradient(" + rotation(viewport) + "rgba(0, 0, 0, 0.2) 1px, transparent 1px)";

        element.style.backgroundPosition = "4px 4px, 4px 4px";

        int weakLinesInterval = WEAK_LINE_INTERVAL * viewport.pixelSize;
        int strongLinesInterval = STRONG_LINE_INTERVAL * viewport.pixelSize;

        element.style.backgroundSize =
                px(weakLinesInterval) + px(weakLinesInterval) + ", " + px(strongLinesInterval) + px(strongLinesInterval);
    }

    private String rotation(final Viewport viewport) {
        return viewport.orientation.equals(VERTICAL) ? "" : "90deg, ";
    }

    public String px(final Object object) {
        return object + "px ";
    }
}
