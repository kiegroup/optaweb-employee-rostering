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

import java.time.Duration;
import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.GridObject;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.gwtui.client.viewport.impl.AbstractHasTimeslotGridObject;
import org.optaweb.employeerostering.gwtui.client.viewport.powers.ClickableDecorator;
import org.optaweb.employeerostering.gwtui.client.viewport.powers.DraggabilityDecorator;
import org.optaweb.employeerostering.gwtui.client.viewport.powers.ResizabilityDecorator;
import org.optaweb.employeerostering.shared.common.HasTimeslot;
import org.optaweb.employeerostering.shared.rotation.RotationRestServiceBuilder;
import org.optaweb.employeerostering.shared.rotation.view.ShiftTemplateView;

@Templated
public class ShiftTemplateGridObject extends AbstractHasTimeslotGridObject<RotationMetadata> implements GridObject<LocalDateTime, RotationMetadata> {

    private ShiftTemplateModel model;
    private ShiftTemplateView shiftTemplateView;

    @Inject
    @DataField("label")
    @Named("span")
    private HTMLElement label;

    @Inject
    private DraggabilityDecorator<LocalDateTime, RotationMetadata> draggability;
    @Inject
    private ResizabilityDecorator<LocalDateTime, RotationMetadata> resizability;
    @Inject
    private ClickableDecorator<LocalDateTime, RotationMetadata> clickable;
    @Inject
    private ManagedInstance<ShiftTemplateEditForm> shiftTemplateEditFormFactory;

    public void withShiftTemplateModel(ShiftTemplateModel model) {
        this.model = model;
        ShiftTemplateView newShift = new ShiftTemplateView();
        newShift.setId(model.getShiftTemplateView().getId());
        newShift.setVersion(model.getShiftTemplateView().getVersion());
        newShift.setRotationEmployeeId(model.getShiftTemplateView().getRotationEmployeeId());
        this.shiftTemplateView = newShift;

        if (getLane() != null) {
            label.innerHTML = (newShift.getRotationEmployeeId() != null)
                    ? new SafeHtmlBuilder().appendEscaped(getLane().getMetadata()
                                                                  .getEmployeeIdToEmployeeMap()
                                                                  .get(newShift.getRotationEmployeeId())
                                                                  .getName()).toSafeHtml().asString()
                    : "Unassigned";
            updatePositionInLane();
        }
    }

    @Override
    public void setStartPositionInScaleUnits(LocalDateTime newStartPosition) {
        updateStartDateTimeWithoutRefresh(newStartPosition);
        model.refreshTwin(this);
    }

    @Override
    public void setEndPositionInScaleUnits(LocalDateTime newEndPosition) {
        updateEndDateTimeWithoutRefresh(newEndPosition);
        model.refreshTwin(this);
    }

    @Override
    public void init(Lane<LocalDateTime, RotationMetadata> lane) {
        updatePositionInLane();
        if (shiftTemplateView != null) {
            label.innerHTML = (shiftTemplateView.getRotationEmployeeId() != null)
                    ? new SafeHtmlBuilder().appendEscaped(getLane().getMetadata()
                                                                  .getEmployeeIdToEmployeeMap()
                                                                  .get(shiftTemplateView.getRotationEmployeeId())
                                                                  .getName()).toSafeHtml().asString()
                    : "Unassigned";
        }
        draggability.applyFor(this, lane.getScale());
        resizability.applyFor(this, lane.getScale());
        clickable.applyFor(this).onClick(this::onClick);
    }

    private void updatePositionInLane() {
        Long daysInRotation = getDaysInRotation();
        LocalDateTime baseDate = (model.isLaterTwin(this)) ? getLane().getScale().getStartInScaleUnits() : getLane().getScale().getStartInScaleUnits()
                .minusDays(daysInRotation);
        updateStartDateTimeWithoutRefresh(baseDate.plus(model.getShiftTemplateView().getDurationBetweenRotationStartAndTemplateStart()));
        updateEndDateTimeWithoutRefresh(getStartPositionInScaleUnits().plus(model.getShiftTemplateView().getShiftTemplateDuration()));
    }

    @Override
    public Long getId() {
        return shiftTemplateView.getId();
    }

    protected void updateStartDateTimeWithoutRefresh(LocalDateTime newStartDateTime) {
        shiftTemplateView.setDurationBetweenRotationStartAndTemplateStart(Duration.between(getLane().getScale().getStartInScaleUnits(),
                                                                                           newStartDateTime));
    }

    protected void updateEndDateTimeWithoutRefresh(LocalDateTime newEndDateTime) {
        shiftTemplateView.setShiftTemplateDuration(Duration.between(getStartPositionInScaleUnits(),
                                                                    newEndDateTime));
    }

    protected Long getDaysInRotation() {
        return Duration.between(getLane().getScale().getStartInScaleUnits(),
                                getLane().getScale().getEndInScaleUnits()).getSeconds() / 60 / 60 / 24;
    }

    protected void reposition() {
        if (getLane() != null) {
            getLane().positionGridObject(this);
        }
    }

    private void onClick(MouseEvent e) {
        if (e.shiftKey) {
            getLane().removeGridObject(model);
        } else {
            shiftTemplateEditFormFactory.get().init(this);
        }
    }

    @Override
    protected HasTimeslot getTimeslot() {
        return shiftTemplateView;
    }

    public ShiftTemplateModel getShiftTemplateModel() {
        return model;
    }

    public void setSelected(boolean isSelected) {
        if (isSelected) {
            getElement().classList.add("selected");
        } else {
            getElement().classList.remove("selected");
        }
    }

    @Override
    public void save() {
        RotationRestServiceBuilder.updateShiftTemplate(model.getShiftTemplateView().getTenantId(), model.getShiftTemplateView(),
                                                       FailureShownRestCallback.onSuccess(stv -> {
                                                           model.withShiftTemplateView(stv);
                                                       }));
    }
}
