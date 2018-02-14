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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.context.Dependent;

@Dependent
public class ListElementViewPool<T extends ListElementView<?>> {

    private final Set<T> availableInstances = new HashSet<>();

    private Long maxSize;
    private Supplier<T> instances;

    public void init(final Long maxSize,
                     final Supplier<T> factory) {

        this.maxSize = maxSize;
        this.instances = factory;
    }

    public T get() {
        final T view = getInstance();
        availableInstances.remove(view);
        view.onDestroy(() -> {
            if (availableInstances.size() < maxSize) {
                availableInstances.add(view);
            }
        });
        return view;
    }

    private T getInstance() {
        if (availableInstances.size() > 0) {
            return availableInstances.stream().findAny().get();
        } else {
            return instances.get();
        }
    }
}
