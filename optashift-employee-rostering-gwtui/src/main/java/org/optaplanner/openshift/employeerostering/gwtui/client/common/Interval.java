package org.optaplanner.openshift.employeerostering.gwtui.client.common;

/**
 * Interval of the form [start,end) (includes start, excludes end)
 */
public final class Interval {

    final EndPoint startPoint;
    final EndPoint endPoint;

    public Interval(EndPoint start, EndPoint end) {
        this.startPoint = start;
        this.endPoint = end;
    }

    public EndPoint getStartPoint() {
        return startPoint;
    }

    public EndPoint getEndPoint() {
        return endPoint;
    }

    @Override
    public String toString() {
        return "[" + startPoint.getLocation() + ", " + endPoint.getLocation() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Interval) {
            Interval other = (Interval) o;
            return this.getStartPoint().equals(other.getStartPoint()) &&
                    this.getEndPoint().equals(other.getEndPoint());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (31 * startPoint.hashCode()) ^ (endPoint.hashCode());
    }
}