package org.optaplanner.openshift.employeerostering.server.rest;

import java.util.List;

import javax.inject.Inject;

import org.optaplanner.openshift.employeerostering.server.roster.RosterDao;
import org.optaplanner.openshift.employeerostering.shared.domain.Roster;
import org.optaplanner.openshift.employeerostering.shared.rest.RosterRestService;

public class RosterRestServiceImpl implements RosterRestService {

    @Inject
    private RosterDao rosterDao;

    @Override
    public Roster getRoster(Long id) {
        return rosterDao.getRoster(id);
    }

    @Override
    public List<Roster> getRosterList() {
        return rosterDao.getRosterList();
    }

}
