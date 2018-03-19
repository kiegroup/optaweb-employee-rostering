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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;

import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.SubLane;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Dependent
public class CollisionFreeSubLaneFactory {

    public <T> List<SubLane<T>> createSubLanes(final Stream<Blob<T>> blobs) {
        return blobs.sorted(comparing(Blob::getPositionInGridPixels))
                .map(this::singletonSubLaneList)
                .reduce(this::merge)
                .orElseGet(() -> new ArrayList<>(Arrays.asList(new SubLane<>())));
    }

    private <T> List<SubLane<T>> singletonSubLaneList(final Blob<T> blob) {
        final List<SubLane<T>> subLaneSingletonList = new ArrayList<>();
        subLaneSingletonList.add(new SubLane<>(blob.toStream().collect(toList())));
        return subLaneSingletonList;
    }

    private <T> List<SubLane<T>> merge(final List<SubLane<T>> lhs,
                                       final List<SubLane<T>> rhs) {

        final List<Blob<T>> rhsBlobs = rhs.get(0).getBlobs();

        final Optional<SubLane<T>> subLaneWithSpace = lhs.stream()
                .filter(candidate -> noneCollideWithEdgeBlobs(rhsBlobs, candidate))
                .findFirst();

        if (subLaneWithSpace.isPresent()) {
            subLaneWithSpace.get().getBlobs().addAll(rhsBlobs);
            return lhs;
        } else {
            lhs.addAll(rhs);
            return lhs;
        }
    }

    private <T> boolean noneCollideWithEdgeBlobs(final List<Blob<T>> blobs,
                                                 final SubLane<T> subLane) {

        final Blob<T> firstBlob = subLane.getBlobs().get(0);
        final Blob<T> lastBlob = subLane.getBlobs().get(subLane.getBlobs().size() - 1);
        return blobs.stream().noneMatch(b -> firstBlob.collidesWith(b) || lastBlob.collidesWith(b));
    }
}
