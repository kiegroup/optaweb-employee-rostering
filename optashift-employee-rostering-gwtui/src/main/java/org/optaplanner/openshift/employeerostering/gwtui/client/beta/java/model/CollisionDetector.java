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

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model;

import java.util.List;
import java.util.function.Supplier;

public class CollisionDetector<T extends Blob<?>> {

    private final Supplier<List<T>> blobs;

    CollisionDetector(final Supplier<List<T>> blobs) {
        this.blobs = blobs;
    }

    public final boolean collides(final T blob) {
        return blobs.get().stream()
                .filter(b -> !b.equals(blob))
                .anyMatch(b -> b.collidesWith(blob));
    }
}
