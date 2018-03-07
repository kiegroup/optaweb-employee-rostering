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

package org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;

import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.SubLane;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

@Dependent
public class CollisionFreeSubLaneBuilder {

    public <T> List<SubLane<T>> buildSubLanes(final Stream<Blob<T>> shiftStream) {

        return shiftStream
                .map(blob -> {
                    final List<SubLane<T>> l = new ArrayList<>();
                    l.add(new SubLane<>(blob.toStream().collect(toList())));
                    return l;
                })
                .reduce(this::merge)
                .orElseGet(Collections::emptyList);
    }

    private <T> List<SubLane<T>> merge(final List<SubLane<T>> lhs,
                                       final List<SubLane<T>> rhs) {

        final Optional<SubLane<T>> subLaneWithSpace = lhs.stream()
                .filter(subLane -> rhs.stream().noneMatch(subLane::collidesWith))
                .findFirst();

        if (subLaneWithSpace.isPresent()) {
            subLaneWithSpace.get().getBlobs().addAll(rhs.get(0).getBlobs());
            return lhs;
        }

        lhs.addAll(rhs);
        return lhs;
    }
}
