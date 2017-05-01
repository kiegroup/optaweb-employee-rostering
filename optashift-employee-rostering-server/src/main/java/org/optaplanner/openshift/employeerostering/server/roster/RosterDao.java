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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.optaplanner.openshift.employeerostering.shared.domain.Roster;

@ApplicationScoped
public class RosterDao {

    private AtomicLong rosterIdGenerator = new AtomicLong(0L);
    // TODO do not fake a database, but connect to one
    private ConcurrentMap<Long, Roster> idToRosterMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeDatabase() {
        RosterGenerator rosterGenerator = new RosterGenerator();
        addRoster(rosterGenerator.generateRoster(10, 7, false));
//        addRoster(rosterGenerator.generateRoster(10, 28, false));
//        addRoster(rosterGenerator.generateRoster(20, 28, false));
//        addRoster(rosterGenerator.generateRoster(40, 28 * 2, false));
//        addRoster(rosterGenerator.generateRoster(80, 28 * 4, false));
//        addRoster(rosterGenerator.generateRoster(10, 28, true));
//        addRoster(rosterGenerator.generateRoster(20, 28, true));
//        addRoster(rosterGenerator.generateRoster(40, 28 * 2, true));
        addRoster(rosterGenerator.generateRoster(80, 28 * 4, true));
    }


    public Long addRoster(Roster roster) {
        Long id = rosterIdGenerator.getAndIncrement();
        // TODO
        // roster.setId(id);
        idToRosterMap.put(id, roster);
        return id;
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

}
