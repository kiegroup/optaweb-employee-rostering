/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.pages.rotation;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import org.optaweb.employeerostering.gwtui.client.viewport.grid.GridObject;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.HasGridObjects;
import org.optaweb.employeerostering.shared.rotation.view.ShiftTemplateView;

public class ShiftTemplateModel implements HasGridObjects<LocalDateTime, RotationMetadata> {

    @Inject
    private ShiftTemplateGridObject earilerTwin;
    @Inject
    private ShiftTemplateGridObject laterTwin;

    private ShiftTemplateView shiftTemplateView;

    @Override
    public Long getId() {
        return shiftTemplateView.getId();
    }

    @Override
    public Collection<GridObject<LocalDateTime, RotationMetadata>> getGridObjects() {
        return Arrays.asList(earilerTwin, laterTwin);
    }

    public ShiftTemplateModel withShiftTemplateView(ShiftTemplateView shiftTemplateView) {
        this.shiftTemplateView = shiftTemplateView;
        earilerTwin.withShiftTemplateModel(this);
        laterTwin.withShiftTemplateModel(this);
        return this;
    }

    public ShiftTemplateView getShiftTemplateView() {
        return shiftTemplateView;
    }

    public boolean isLaterTwin(ShiftTemplateGridObject view) {
        return laterTwin == view;
    }

    public void setLaterTwin(ShiftTemplateGridObject view) {
        if (laterTwin != view) {
            earilerTwin = laterTwin;
            laterTwin = view;
        }
    }

    public void setEarilerTwin(ShiftTemplateGridObject view) {
        if (earilerTwin != view) {
            laterTwin = earilerTwin;
            earilerTwin = view;
        }
    }

    public void refreshTwin(ShiftTemplateGridObject view) {
        if (view != earilerTwin && view.getStartPositionInScaleUnits().isBefore(view.getLane().getScale().getStartInScaleUnits())) {
            laterTwin = earilerTwin;
            earilerTwin = view;
        } else if (view != laterTwin && !view.getStartPositionInScaleUnits().isBefore(view.getLane().getScale().getStartInScaleUnits())) {
            earilerTwin = laterTwin;
            laterTwin = view;
        }

        if (view == earilerTwin) {
            laterTwin.updateStartDateTimeWithoutRefresh(view.getStartPositionInScaleUnits().plusDays(view.getDaysInRotation()));
            laterTwin.updateEndDateTimeWithoutRefresh(view.getEndPositionInScaleUnits().plusDays(view.getDaysInRotation()));
            laterTwin.reposition();
        } else {
            shiftTemplateView.setDurationBetweenRotationStartAndTemplateStart(view.getTimeslot().getDurationBetweenReferenceAndStart());
            shiftTemplateView.setShiftTemplateDuration(view.getTimeslot().getDurationOfTimeslot());
            earilerTwin.updateStartDateTimeWithoutRefresh(view.getStartPositionInScaleUnits().minusDays(view.getDaysInRotation()));
            earilerTwin.updateEndDateTimeWithoutRefresh(view.getEndPositionInScaleUnits().minusDays(view.getDaysInRotation()));
            earilerTwin.reposition();
        }
    }
}
