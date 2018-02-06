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

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.GridLineView.Strength.STRONG;
import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.GridLineView.Strength.WEAK;

@Dependent
public class DefaultGridLines {

    @Inject
    private ListView<GridLine> gridLines;

    @Inject
    private ManagedInstance<GridLineView> gridLineInstances;

    private static final Integer WEAK_LINE_INTERVAL = 1;
    private static final Integer STRONG_LINE_INTERVAL = 12;

    public void draw(final IsElement container, final Viewport viewport) {

        final List<GridLine> list = new ArrayList<>();

        for (int i = 1; i < viewport.sizeInPixels; i++) {
            if (i % STRONG_LINE_INTERVAL == 0) {
                list.add(new GridLine(i, STRONG));
            } else if (i % WEAK_LINE_INTERVAL == 0) {
                list.add(new GridLine(i, WEAK));
            }
        }

        gridLines.init(container.getElement(), list, () -> gridLineInstances.get().withViewport(viewport));
    }
}
