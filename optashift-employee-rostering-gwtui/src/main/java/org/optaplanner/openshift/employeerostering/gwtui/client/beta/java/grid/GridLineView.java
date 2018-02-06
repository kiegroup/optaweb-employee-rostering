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

import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListElementView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;

@Templated
public class GridLineView implements ListElementView<GridLine> {

    private static final Integer OFFSET = 6; // in screen pixels;

    private Viewport viewport;

    @Override
    public ListElementView<GridLine> setup(final GridLine gridLine,
                                           final ListView<GridLine> list) {
        switch (gridLine.getStrength()) {
            case WEAK:
                getElement().classList.add("weak");
                break;
            case STRONG:
                getElement().classList.add("strong");
                break;
        }

        viewport.position(this, gridLine.getIndex(), OFFSET);
        return this;
    }

    public ListElementView<GridLine> withViewport(final Viewport viewport) {
        this.viewport = viewport;
        return this;
    }

    public enum Strength {
        WEAK,
        STRONG;
    }
}
