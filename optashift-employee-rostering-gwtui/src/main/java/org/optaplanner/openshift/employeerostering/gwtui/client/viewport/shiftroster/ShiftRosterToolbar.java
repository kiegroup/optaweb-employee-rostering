package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.shiftroster;

import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.RosterToolbar;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.view.ShiftRosterView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_PAGINATION;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_INVALIDATE;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_UPDATE;

@Templated
public class ShiftRosterToolbar extends RosterToolbar implements IsElement {

    @Override
    protected Event<ShiftRosterView> getViewRefreshEvent() {
        return SHIFT_ROSTER_UPDATE;
    }

    @Override
    protected Event<Pagination> getPageChangeEvent() {
        return SHIFT_ROSTER_PAGINATION;
    }

    @Override
    protected Event<Void> getViewInvalidateEvent() {
        return SHIFT_ROSTER_INVALIDATE;
    }
    
}
