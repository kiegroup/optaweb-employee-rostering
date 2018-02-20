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

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers;

import java.util.Optional;
import java.util.stream.Stream;

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;

public interface BlobWithTwin<T, Y extends BlobWithTwin<T, Y>> extends Blob<T> {

    Y getUpdatedTwin();

    Y newTwin();

    Optional<Y> getTwin();

    void setTwin(final Y twin);

    default Stream<Blob<T>> toStream() {
        if (!getTwin().isPresent()) {
            return Stream.of(this);
        } else {
            return Stream.of(this, getTwin().get());
        }
    }
}
