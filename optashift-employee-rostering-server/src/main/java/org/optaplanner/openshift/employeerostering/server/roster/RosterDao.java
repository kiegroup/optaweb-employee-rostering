/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.server.roster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.optaplanner.openshift.employeerostering.shared.roster.Roster;

@ApplicationScoped
public class RosterDao {

    // TODO do not fake a database, but connect to one
    private ConcurrentMap<Long, Roster> idToRosterMap = new ConcurrentHashMap<>();

    @Inject
    private RosterGenerator rosterGenerator;

    @PostConstruct
    public void setUpGeneratedData() {
        addGeneratedRoster(rosterGenerator.generateRoster(10, 7, false));
//        addGeneratedRoster(rosterGenerator.generateRoster(10, 28, false));
//        addGeneratedRoster(rosterGenerator.generateRoster(20, 28, false));
//        addGeneratedRoster(rosterGenerator.generateRoster(40, 28 * 2, false));
//        addGeneratedRoster(rosterGenerator.generateRoster(80, 28 * 4, false));
//        addGeneratedRoster(rosterGenerator.generateRoster(10, 28, true));
//        addGeneratedRoster(rosterGenerator.generateRoster(20, 28, true));
//        addGeneratedRoster(rosterGenerator.generateRoster(40, 28 * 2, true));
        addGeneratedRoster(rosterGenerator.generateRoster(80, 28 * 4, true));
    }

    private void addGeneratedRoster(Roster roster) {
        Long id = roster.getId();
        Roster old = idToRosterMap.putIfAbsent(id, roster);
        if (old != null) {
            throw new IllegalArgumentException("Cannot add roster (" + roster
                    + ") because is already a roster (" + old + ") with id (" + id + ").");
        }
    }

    public List<Roster> getRosterList() {
        // Defensive copy
        return new ArrayList<>(idToRosterMap.values());
    }

    public Roster getRoster(Long id) {
        Roster roster = idToRosterMap.get(id);
        if (roster == null) {
            throw new IllegalArgumentException("There is no roster with id (" + id + ").");
        }
        return roster;
    }

    public void updateRoster(Roster roster) {
        idToRosterMap.put(roster.getId(), roster);
    }

}
