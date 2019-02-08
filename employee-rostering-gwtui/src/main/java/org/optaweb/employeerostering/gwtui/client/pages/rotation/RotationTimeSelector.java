package org.optaweb.employeerostering.gwtui.client.pages.rotation;

import java.time.LocalTime;

import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.IntegerBox;
import org.gwtbootstrap3.client.ui.form.validator.DecimalMaxValidator;
import org.gwtbootstrap3.client.ui.form.validator.DecimalMinValidator;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.LocalTimePicker;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaweb.employeerostering.shared.roster.RosterState;

@Templated
public class RotationTimeSelector {

    @DataField("day-offset")
    private IntegerBox dayOffsetPicker;

    @DataField("time")
    private LocalTimePicker timePicker;

    private int rotationLength;

    private int offset;

    @Inject
    public RotationTimeSelector(IntegerBox dayOffsetPicker, LocalTimePicker timePicker, TenantStore tenantStore) {
        offset = 0;
        this.dayOffsetPicker = dayOffsetPicker;
        this.timePicker = timePicker;
        RosterRestServiceBuilder.getRosterState(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(this::setRotationLength));
    }

    protected void setRotationLength(RosterState rs) {
        rotationLength = rs.getRotationLength();
        dayOffsetPicker.addValidator(new DecimalMinValidator<Integer>(1));
        dayOffsetPicker.addValidator(new DecimalMaxValidator<Integer>(rotationLength));
        setDayOffset(offset);
    }

    public void setDayOffset(int dayOffset) {
        offset = dayOffset;
        dayOffsetPicker.setValue((dayOffset % rotationLength) + 1);
    }

    public void setTime(LocalTime time) {
        timePicker.setValue(time);
    }

    public int getDayOffset() {
        return dayOffsetPicker.getValue() - 1;
    }

    public int getRotationLength() {
        return rotationLength;
    }

    public LocalTime getTime() {
        return timePicker.getValue();
    }
}
