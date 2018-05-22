package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.spotroster;

import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.RosterToolbar;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SPOT_ROSTER_PAGINATION;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SPOT_ROSTER_INVALIDATE;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SPOT_ROSTER_UPDATE;

@Templated
public class SpotRosterToolbar extends RosterToolbar implements IsElement {

    @Override
    protected Event<SpotRosterView> getViewRefreshEvent() {
        return SPOT_ROSTER_UPDATE;
    }

    @Override
    protected Event<Pagination> getPageChangeEvent() {
        return SPOT_ROSTER_PAGINATION;
    }

    @Override
    protected Event<Void> getViewInvalidateEvent() {
        return SPOT_ROSTER_INVALIDATE;
    }

}
