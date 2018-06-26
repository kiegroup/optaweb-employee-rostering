package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.availabilityroster;

import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.LocalDateRange;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.RosterToolbar;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.view.AvailabilityRosterView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.AVAILABILITY_ROSTER_DATE_RANGE;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.AVAILABILITY_ROSTER_INVALIDATE;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.AVAILABILITY_ROSTER_PAGINATION;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.AVAILABILITY_ROSTER_UPDATE;

@Templated
public class AvailabilityRosterToolbar extends RosterToolbar implements IsElement {

    @Override
    protected Event<AvailabilityRosterView> getViewRefreshEvent() {
        return AVAILABILITY_ROSTER_UPDATE;
    }

    @Override
    protected Event<Pagination> getPageChangeEvent() {
        return AVAILABILITY_ROSTER_PAGINATION;
    }

    @Override
    protected Event<Void> getViewInvalidateEvent() {
        return AVAILABILITY_ROSTER_INVALIDATE;
    }

    @Override
    protected Event<LocalDateRange> getDateRangeEvent() {
        return AVAILABILITY_ROSTER_DATE_RANGE;
    }

}
