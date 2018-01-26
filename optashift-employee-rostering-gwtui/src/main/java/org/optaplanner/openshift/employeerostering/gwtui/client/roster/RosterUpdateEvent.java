package org.optaplanner.openshift.employeerostering.gwtui.client.roster;

import org.optaplanner.openshift.employeerostering.shared.roster.view.AbstractRosterView;

public final class RosterUpdateEvent {

    private final AbstractRosterView roster;

    public RosterUpdateEvent(AbstractRosterView roster) {
        this.roster = roster;
    }

    public AbstractRosterView getRoster() {
        return roster;
    }
}
