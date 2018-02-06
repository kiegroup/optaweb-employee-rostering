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

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;

public class ListView<Y> {

    private Supplier<ListElementView<Y>> viewSupplier;
    private HTMLElement container;

    private List<Y> objects;
    private Map<Y, ListElementView<Y>> views;

    public void init(final HTMLElement container,
                     final List<Y> lanes,
                     final Supplier<ListElementView<Y>> viewFactory) {

        this.container = container;
        this.viewSupplier = viewFactory;
        this.objects = new ArrayList<>();
        this.views = new HashMap<>();
        addAll(lanes);
    }

    public void addAll(final List<Y> newObjects) {
        for (final Y obj : newObjects) {
            objects.add(obj);
            final ListElementView<Y> elementView = viewSupplier.get().setup(obj, this);
            views.put(obj, elementView);
            container.appendChild(elementView.getElement());
        }
    }

    public void add(final Y newObject) {
        addAll(singletonList(newObject));
    }

    public void remove(final Y object) {
        final ListElementView<Y> view = views.remove(object);
        container.removeChild(view.getElement());
        objects.remove(object);
    }

    public List<Y> getObjects() {
        return objects;
    }
}
