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
