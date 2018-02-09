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

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.demo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.FiniteLinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.SubLane;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class SpotLane extends Lane<LocalDateTime> {

    private Spot spot;

    // Changing the parameter name to 'spot' leads to an error, because it uses the field instead of the parameter.
    public SpotLane(final FiniteLinearScale<LocalDateTime> scale,
                    final Spot spotParam,
                    final Map<ShiftView, TimeSlot> shiftViewsParam) {

        super(spotParam.getName(), buildSubLanes(scale, spotParam, shiftViewsParam));
        this.spot = spotParam;
    }

    private static ArrayList<SubLane<LocalDateTime>> buildSubLanes(final FiniteLinearScale<LocalDateTime> scale, final Spot spot, final Map<ShiftView, TimeSlot> shiftViewsParam) {

        //FIXME: Handle overlapping blobs and discover why some TimeSlots are null

        final List<Blob<LocalDateTime>> blobs = shiftViewsParam.entrySet()
                .stream()
                .filter(s -> s.getValue() != null)
                .map(s -> new ShiftBlob(new Shift(s.getKey(), spot, s.getValue()), scale))
                .collect(toList());

        return new ArrayList<>(singletonList(new SubLane<>(blobs)));
    }

    public Spot getSpot() {
        return spot;
    }
}
