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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.spotroster;

import java.time.OffsetDateTime;

import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListElementView;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.roster.view.IndictmentView;

@Templated
public class ShiftBlobView implements BlobView<OffsetDateTime, ShiftBlob> {

    private static final Long BLOB_POSITION_DISPLACEMENT_IN_SCREEN_PIXELS = 3L;
    private static final Long BLOB_SIZE_DISPLACEMENT_IN_SCREEN_PIXELS = -5L;

    @Inject
    @DataField("blob")
    private HTMLDivElement root;

    @Inject
    @Named("span")
    @DataField("label")
    private HTMLElement label;

    @Inject
    private SpotRosterPage page;

    private Viewport<OffsetDateTime> viewport;
    private ListView<ShiftBlob> blobViews;
    private Runnable onDestroy;

    private ShiftBlob blob;
    private IndictmentView indictment;

    @Override
    public ListElementView<ShiftBlob> setup(final ShiftBlob blob,
                                            final ListView<ShiftBlob> blobViews) {

        this.blobViews = blobViews;
        this.blob = blob;

        refresh();

        // FIXME: Enable draggability and resizability after backend supports it.

        return this;
    }

    public void refresh() {
        indictment = page.getCurrentShiftIndictmentMap().get(blob.getShift().getId());

        setClassProperty("pinned", blob.getShift().isPinnedByUser());
        setClassProperty("unassigned", blob.getShift().getEmployee() == null);
        RosterState rosterState = page.getCurrentSpotRosterView().getRosterState();
        setClassProperty("historic", rosterState.isHistoric(blob.getShift()));
        setClassProperty("published", rosterState.isPublished(blob.getShift()));
        setClassProperty("draft", rosterState.isDraft(blob.getShift()));

        viewport.setPositionInScreenPixels(this, blob.getPositionInGridPixels(), BLOB_POSITION_DISPLACEMENT_IN_SCREEN_PIXELS);
        viewport.setSizeInScreenPixels(this, blob.getSizeInGridPixels(), BLOB_SIZE_DISPLACEMENT_IN_SCREEN_PIXELS);

        updateIndictmentIcons();
        updateLabel();
    }

    private void setClassProperty(String clazz, boolean isSet) {
        if (isSet) {
            getElement().classList.add(clazz);
        } else {
            getElement().classList.remove(clazz);
        }
    }

    private boolean onResize(final Long newSizeInGridPixels) {
        blob.setSizeInGridPixels(newSizeInGridPixels);
        //TODO: Update Shift's time slot
        //ShiftRestServiceBuilder.updateShift(blob.getShift().getTenantId(), new ShiftView(blob.getShift()));

        updateLabel();
        return true;
    }

    private boolean onDrag(final Long newPositionInGridPixels) {
        blob.setPositionInScaleUnits(viewport.getScale().toScaleUnits(newPositionInGridPixels));
        //TODO: Update Shift's time slot
        //ShiftRestServiceBuilder.updateShift(blob.getShift().getTenantId(), new ShiftView(blob.getShift()));

        updateLabel();
        return true;
    }

    @EventHandler("blob")
    public void onBlobClicked(final @ForEvent("click") MouseEvent e) {
        page.getBlobPopover().showFor(this);
    }

    private void updateLabel() {
        label.textContent = blob.getLabel();
    }

    private void updateIndictmentIcons() {
        setClassProperty("unknown-rule", false);
        setClassProperty("desired-timeslot", false);
        setClassProperty("multiple-shifts-on-a-single-day", false);
        setClassProperty("shifts-less-than-10-hours-apart", false);
        setClassProperty("employee-is-not-the-rotation-employee", false);
        setClassProperty("missing-required-skill", false);
        setClassProperty("unavaliable-timeslot", false);
        setClassProperty("undesired-timeslot", false);

        if (null != indictment) {
            setBlobColorAccordingToScore(indictment.getScoreList().stream().reduce((a, b) -> a.add(b)).get());
            for (IndictmentView.Constraint constraint : indictment.getConstraintMatchedList()) {
                switch (constraint) {
                    case DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE:
                        setClassProperty("desired-time-slot-for-an-employee", true);
                        break;
                    case AT_MOST_ONE_SHIFT_ASSIGNMENT_PER_DAY_PER_EMPLOYEE:
                        setClassProperty("at-most-one-shift-assignment-per-day-per-employee", false);
                        break;
                    case NO_2_SHIFTS_WITHIN_10_HOURS_FROM_EACH_OTHER:
                        setClassProperty("no-2-shifts-within-10-hours-from-each-other", true);
                        break;
                    case EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE:
                        setClassProperty("employee-is-not-rotation-employee", true);
                        break;
                    case REQUIRED_SKILL_FOR_A_SHIFT:
                        setClassProperty("required-skill-for-a-shift", true);
                        break;
                    case UNAVALIABLE_TIMESLOT_FOR_EMPLOYEE:
                        setClassProperty("unavaliable-time-slot-for-an-employee", true);
                        break;
                    case UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE:
                        setClassProperty("undesired-time-slot-for-an-employee", true);
                        break;
                }
            }
        } else {
            setBlobColorAccordingToScore(HardSoftScore.ZERO);
        }

    }

    @Override
    public BlobView<OffsetDateTime, ShiftBlob> withViewport(final Viewport<OffsetDateTime> viewport) {
        this.viewport = viewport;
        return this;
    }

    @Override
    public BlobView<OffsetDateTime, ShiftBlob> withSubLane(final SubLane<OffsetDateTime> subLaneView) {
        return this;
    }

    @Override
    public void onDestroy(final Runnable onDestroy) {
        this.onDestroy = onDestroy;
    }

    @Override
    public void destroy() {
        onDestroy.run();
    }

    public void remove() {
        blobViews.remove(blob);
    }

    @Override
    public Blob<OffsetDateTime> getBlob() {
        return blob;
    }

    private void setBlobColorAccordingToScore(HardSoftScore score) {
        if (score.getHardScore() != 0) {
            if (score.getHardScore() > 0) {
                // maps positive values (75, 100)
                setAnimationDelay(mapToRange(score.getHardScore(), 50, 100));
            } else {
                // maps negative values (0, 25)
                setAnimationDelay(mapToRange(score.getHardScore(), 0, 50));
            }
        } else {
            // 0 get mapped to 50
            setAnimationDelay(mapToRange(score.getSoftScore(), 25, 75));
        }
    }

    private double mapToRange(double amount, double start, double end) {
        return (end - start) * (Math.atan(amount) + Math.PI / 2) / Math.PI + start;
    }

    private void setAnimationDelay(double animationDelay) {
        // Due to CSS not allowing you to set animation-frame or something similar, we set
        // it to an NEGATIVE animation-delay with the animation being paused, so it will
        // begin that much ahead in the animation (so we are effectively setting the frame).
        // animation-duration should be 100s
        getElement().style.set("animation-delay", "-" + animationDelay + "s");
    }

}
