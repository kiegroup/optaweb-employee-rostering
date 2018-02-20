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

public class ListView<T> {

    private Supplier<ListElementView<T>> viewFactory;
    private HTMLElement container;

    private List<T> objects;
    private Map<T, ListElementView<T>> views;

    private boolean init = false;

    public void init(final HTMLElement container,
                     final List<T> objects,
                     final Supplier<ListElementView<T>> viewFactory) {

        reset();
        this.init = true;
        this.container = container;
        this.viewFactory = viewFactory;
        this.objects = new ArrayList<>();
        this.views = new HashMap<>();
        addAll(objects);
    }

    private void reset() {
        if (init) {
            // Has to be a copy because the remove method removes things from the objects list.
            new ArrayList<>(objects).forEach(this::remove);
        }
    }

    public void addAll(final List<T> objects) {
        objects.forEach(this::add);
    }

    public void addAfter(final T object, final T newObject) {
        final ListElementView<T> view = viewFactory.get().setup(newObject, this);
        views.put(newObject, view);

        int next = objects.indexOf(object) + 1;

        if (next <= 0 || next >= objects.size()) {
            container.appendChild(view.getElement());
            objects.add(newObject);
        } else {
            container.insertBefore(view.getElement(), views.get(objects.get(next)).getElement());
            objects.add(next, newObject);
        }
    }

    public void add(final T newObject) {
        final ListElementView<T> view = viewFactory.get().setup(newObject, this);
        views.put(newObject, view);
        container.appendChild(view.getElement());
        objects.add(newObject);
    }

    public void remove(final T object) {
        final ListElementView<T> view = views.remove(object);
        view.destroy();
        container.removeChild(view.getElement());
        objects.remove(object);
    }

    public void clear() {
        reset();
    }

    public List<T> getObjects() {
        return objects;
    }

    public boolean isEmpty() {
        return objects.isEmpty();
    }

    public ListElementView<T> getView(final T obj) {
        return views.get(obj);
    }

    public void addIfNotPresent(final T obj) {
        if (!objects.contains(obj)) {
            add(obj);
        }
    }
}
