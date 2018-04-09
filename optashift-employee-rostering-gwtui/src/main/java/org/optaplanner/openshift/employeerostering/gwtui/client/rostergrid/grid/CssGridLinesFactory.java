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

@Dependent
public class CssGridLinesFactory {

    @Inject
    @Named("span")
    private HTMLElement span;

    public CssGridLines newWithSteps(final Long softLineStep,
                                     final Long harshLineStep,
                                     final Long offset) {

        return new CssGridLines(softLineStep, harshLineStep, offset, () -> (HTMLElement) span.cloneNode(false));
    }
}
