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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PageUtils;

@Dependent
public class TicksFactory<T> {

    @Inject
    @Named("span")
    private HTMLElement span;

    @Inject
    private PageUtils pageUtils;

    public Ticks<T> newTicks(final LinearScale<T> scale, String className, final Long position, final Long stepSize, final Long offset) {
        HTMLElement element = (HTMLElement) span.cloneNode(false);
        element.classList.add(className);
        return new Ticks<>(scale, className, position, stepSize, offset, () -> (HTMLElement) element.cloneNode(false), pageUtils);
    }
}
