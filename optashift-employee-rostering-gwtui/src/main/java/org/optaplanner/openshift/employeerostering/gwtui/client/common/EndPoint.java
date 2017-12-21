package org.optaplanner.openshift.employeerostering.gwtui.client.common;

public final class EndPoint implements Comparable<EndPoint> {

    final long location;
    final boolean isEndOfInterval;

    public EndPoint(long location, boolean isEndPoint) {
        this.location = location;
        this.isEndOfInterval = isEndPoint;
    }

    public long getLocation() {
        return this.location;
    }

    public boolean isEndPoint() {
        return isEndOfInterval;
    }

    public boolean isStartPoint() {
        return !isEndOfInterval;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EndPoint) {
            EndPoint other = (EndPoint) o;
            return this.getLocation() == other.getLocation() && this.isEndPoint() == other.isEndPoint();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(location) ^ Boolean.hashCode(isEndOfInterval);
    }

    @Override
    public int compareTo(EndPoint o) {
        int result = Long.compare(this.getLocation(), o.getLocation());
        if (result != 0) {
            return result;
        }
        if (this.isEndPoint() == o.isEndPoint()) {
            return 0;
        } else if (this.isEndPoint()) {
            return 1;
        } else {
            return -1;
        }
    }

}