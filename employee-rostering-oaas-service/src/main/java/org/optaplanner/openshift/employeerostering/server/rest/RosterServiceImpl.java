package org.optaplanner.openshift.employeerostering.server.rest;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.optaplanner.openshift.employeerostering.domain.Person;
import org.optaplanner.openshift.employeerostering.server.generator.RosterGenerator;
import org.optaplanner.openshift.employeerostering.shared.domain.Roster;
import org.optaplanner.openshift.employeerostering.shared.rest.RosterService;

public class RosterServiceImpl implements RosterService {

    @Inject
    private RosterGenerator rosterGenerator;

    @Override
    public Roster getRoster(String id) {
        return rosterGenerator.generateRoster(10, 7, false);
    }

    @Override
    public List<Roster> getRosterList() {
        return Arrays.asList(
                rosterGenerator.generateRoster(10, 7, false),
                rosterGenerator.generateRoster(10, 28, false),
                rosterGenerator.generateRoster(20, 28, false),
                rosterGenerator.generateRoster(40, 28 * 2, false),
                rosterGenerator.generateRoster(80, 28 * 4, false),
                rosterGenerator.generateRoster(10, 28, true),
                rosterGenerator.generateRoster(20, 28, true),
                rosterGenerator.generateRoster(40, 28 * 2, true),
                rosterGenerator.generateRoster(80, 28 * 4, true));
    }

}
