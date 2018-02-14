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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.spotroster;

import java.time.LocalDateTime;
import java.util.List;

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.SubLane;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class SpotLane extends Lane<LocalDateTime> {

    private final Spot spot;

    // Changing the parameter name to 'spot' leads to an error, because it uses the field instead of the parameter.
    public SpotLane(final Spot spotParam,
                    final List<SubLane<LocalDateTime>> subLanes) {

        super(spotParam.getName(), subLanes);
        this.spot = spotParam;
    }

    public Spot getSpot() {
        return spot;
    }
}
