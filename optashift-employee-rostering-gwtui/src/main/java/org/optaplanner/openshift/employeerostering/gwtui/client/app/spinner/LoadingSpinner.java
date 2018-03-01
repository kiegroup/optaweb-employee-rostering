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

package org.optaplanner.openshift.employeerostering.gwtui.client.app.spinner;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
@ApplicationScoped
public class LoadingSpinner implements IsElement {

    private List<String> loadingTasks = new ArrayList<>();

    public void showFor(final String taskId) {
        loadingTasks.add(taskId);
        show();
    }

    public void hideFor(final String taskId) {
        loadingTasks.remove(taskId);
        if (loadingTasks.isEmpty()) {
            hide();
        }
    }

    private void show() {
        getElement().classList.remove("hidden");
    }

    private void hide() {
        getElement().classList.add("hidden");
    }
}
