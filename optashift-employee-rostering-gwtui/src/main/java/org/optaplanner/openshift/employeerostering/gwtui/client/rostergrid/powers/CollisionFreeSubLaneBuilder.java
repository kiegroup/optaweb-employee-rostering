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
import java.util.Collection;
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
                .map(blob -> Stream.of(new SubLane<>(new ArrayList<>(singletonList(blob)))))
                .reduce(Stream.of(), this::merge)
                .map(this::withBlobStream)
                .collect(toList());
    }

    private <T> Stream<SubLane<T>> merge(final Stream<SubLane<T>> lhsStream,
                                         final Stream<SubLane<T>> rhsStream) {

        final List<SubLane<T>> lhs = lhsStream.collect(toList());
        final List<SubLane<T>> rhs = rhsStream.collect(toList());

        final Optional<SubLane<T>> subLaneWithSpace = lhs.stream()
                .filter(subLane -> rhs.stream().map(this::withBlobStream).noneMatch(subLane::collidesWith))
                .findFirst();

        if (subLaneWithSpace.isPresent()) {
            return merge(lhs, rhs, subLaneWithSpace.get());
        }

        return concat(lhs.stream(), rhs.stream());
    }

    private <T> Stream<SubLane<T>> merge(final List<SubLane<T>> lhs,
                                         final List<SubLane<T>> rhs,
                                         final SubLane<T> subLaneWithSpace) {

        final int indexOfSubLaneWithSpace = lhs.indexOf(subLaneWithSpace);

        final List<SubLane<T>> left = lhs.subList(0, indexOfSubLaneWithSpace);
        final List<SubLane<T>> right = lhs.subList(indexOfSubLaneWithSpace + 1, lhs.size());

        final List<Blob<T>> mergedBlobs = concat(Stream.of(subLaneWithSpace), rhs.stream())
                .map(SubLane::getBlobs)
                .flatMap(Collection::stream)
                .collect(toList());

        return concat(concat(left.stream(), Stream.of(new SubLane<>(mergedBlobs))), right.stream());
    }

    private <T> SubLane<T> withBlobStream(final SubLane<T> subLane) {

        final List<Blob<T>> blobs = subLane.getBlobs().stream()
                .flatMap(Blob::toStream)
                .collect(toList());

        return new SubLane<>(blobs);
    }
}
