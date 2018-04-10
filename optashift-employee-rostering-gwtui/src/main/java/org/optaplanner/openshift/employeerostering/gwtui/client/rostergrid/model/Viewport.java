/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model;

import java.util.List;
import java.util.stream.Stream;

import elemental2.dom.MouseEvent;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;

public abstract class Viewport<T> {

    private IsElement mouseTarget;

    public abstract void drawGridLinesAt(final IsElement target);

    public abstract void drawTicksAt(final IsElement target);

    public abstract Lane<T> newLane();

    public abstract Stream<Blob<T>> newBlob(final Lane<T> lane, final T positionInScaleUnits);

    public abstract BlobView<T, ?> newBlobView();

    public abstract List<Lane<T>> getLanes();

    public abstract Long getGridPixelSizeInScreenPixels();

    public abstract Orientation getOrientation();

    public abstract LinearScale<T> getScale();

    // Not neccessary rows, but no better name for it
    public abstract Long getHeaderRows();

    // Not neccessart columns, but no better name for it
    public abstract Long getHeaderColumns();

    public <Y> Y decideBasedOnOrientation(final Y verticalOption, final Y horizontalOption) {
        return getOrientation().equals(Orientation.VERTICAL) ? verticalOption : horizontalOption;
    }

    public Long getSizeInGridPixels(final IsElement element) {
        return toGridPixels(getOrientation().getSize(element));
    }

    public Long toGridPixels(final Long screenPixels) {
        return screenPixels / getGridPixelSizeInScreenPixels();
    }

    public Long toScreenPixels(final Long gridPixels) {
        return gridPixels * getGridPixelSizeInScreenPixels();
    }

    public void setSizeInScreenPixels(final IsElement element, final Long sizeInGridPixels) {
        getOrientation().scale(element, sizeInGridPixels, this);
    }

    public void setGroupSizeInScreenPixels(final IsElement element, final Long sizeInGridPixels) {
        getOrientation().groupScale(element, sizeInGridPixels, this);
    }

    public void setPositionInScreenPixels(final IsElement element, final Long positionInGridPixels) {
        getOrientation().position(element, positionInGridPixels, this);
    }

    public void setAbsPositionInScreenPixels(final IsElement element, final Long positionInGridPixels) {
        getOrientation().absPosition(element, positionInGridPixels, this);
    }

    public void setGroupPosition(final IsElement element, final long groupPosition) {
        getOrientation().groupPosition(element, groupPosition, this);
    }

    public void setAbsGroupPosition(final IsElement element, final long groupPosition) {
        getOrientation().absGroupPosition(element, groupPosition, this);
    }

    public Long getSizeInGridPixels() {
        return getScale().getEndInGridPixels();
    }

    public int getLaneStartPosition(Lane<T> lane) {
        int currIndex = 0;
        for (int i = 0; i < getLanes().size(); i++) {
            if (getLanes().get(i).equals(lane)) {
                return currIndex;
            }
            currIndex += getLanes().get(i).getSubLanes().size();
        }
        return -1;
    }

    public int getLaneEndPosition(Lane<T> lane) {
        int currIndex = 0;
        ;
        for (int i = 0; i < getLanes().size(); i++) {
            if (getLanes().get(i).equals(lane)) {
                return currIndex + getLanes().get(i).getSubLanes().size() - 1;
            }
            currIndex += getLanes().get(i).getSubLanes().size();
        }
        return -1;
    }

    public int getSubLanePosition(SubLane<T> subLane) {
        int currIndex = 0;
        int listIndex = -1;
        for (int i = 0; i < getLanes().size(); i++) {
            if (getLanes().get(i).getTitle().equals(subLane.getLaneTitle())) {
                listIndex = i;
                break;
            }
            currIndex += getLanes().get(i).getSubLanes().size();
        }
        if (listIndex > -1) {
            Lane<T> lane = getLanes().get(listIndex);
            return currIndex + lane.getSubLanes().indexOf(subLane);
        }
        return -1;
    }

    public Long getGroupEndPosition() {
        List<Lane<T>> laneList = getLanes();
        return (laneList.isEmpty()) ? 0L : getLaneEndPosition(laneList.get(laneList.size() - 1));
    }

    public double getGridPositionOfMouse(Blob<T> blob, MouseEvent e) {
        double mouseOffsetFromBlobInScreenPixels = (getOrientation() == Orientation.HORIZONTAL) ? e.offsetX : e.offsetY;
        double mouseOffsetFromBlobInGridPixels = mouseOffsetFromBlobInScreenPixels / getGridPixelSizeInScreenPixels();
        return (blob.getPositionInGridPixels() >= 0) ? blob.getPositionInGridPixels() + mouseOffsetFromBlobInGridPixels : mouseOffsetFromBlobInGridPixels;
    }

    public IsElement getMouseTarget() {
        return mouseTarget;
    }

    public void setMouseTarget(IsElement mouseTarget) {
        this.mouseTarget = mouseTarget;
    }
}
