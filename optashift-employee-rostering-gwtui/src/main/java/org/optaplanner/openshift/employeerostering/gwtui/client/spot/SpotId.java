package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class SpotId implements HasTitle {

    Spot spot;

    public SpotId(Spot spot) {
        this.spot = spot;
    }

    @Override
    public String getTitle() {
        return spot.getName();
    }

    public Spot getSpot() {
        return spot;
    }

    @Override
    public int hashCode() {
        return spot.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SpotId) {
            SpotId other = (SpotId) o;
            return spot.equals(other.getSpot());
        }
        return false;
    }
}
