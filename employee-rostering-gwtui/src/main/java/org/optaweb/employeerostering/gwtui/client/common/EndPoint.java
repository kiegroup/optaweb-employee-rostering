/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.gwtui.client.common;

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
