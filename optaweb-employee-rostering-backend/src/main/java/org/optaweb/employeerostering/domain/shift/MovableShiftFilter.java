/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.optaweb.employeerostering.domain.shift;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.domain.roster.RosterState;

public class MovableShiftFilter implements SelectionFilter<Roster, Shift> {

    @Override
    public boolean accept(ScoreDirector<Roster> scoreDirector, Shift shift) {
        Roster roster = scoreDirector.getWorkingSolution();
        RosterState rosterState = roster.getRosterState();
        if (roster.isNondisruptivePlanning()) {
            return !shift.getStartDateTime().isBefore(roster.getNondisruptiveReplanFrom());
        } else {
            return rosterState.isDraft(shift);
        }
    }
}
