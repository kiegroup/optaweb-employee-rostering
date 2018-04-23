package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;
import elemental2.dom.HTMLElement;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.base.constants.DateTimePickerView;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.DateTimeUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.DateTimeUtils.MomentZoneId;
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;

@Templated
public class DateTimeSelector implements TakesValue<OffsetDateTime> {

    @Inject
    @DataField("date-time-picker")
    private DateTimePicker dateTimePicker;

    @Inject
    @DataField("time-zone-display")
    @Named("span")
    private HTMLElement timeZoneDisplay;

    @Inject
    private DateTimeUtils dateTimeUtils;

    private MomentZoneId momentZoneId;

    @PostConstruct
    private void init() {
        momentZoneId = dateTimeUtils.getTenantZoneId();
        dateTimePicker.setAutoClose(false);
        dateTimePicker.setAllowBlank(false);
        dateTimePicker.setHasKeyboardNavigation(true);
        dateTimePicker.setHighlightToday(true);
        dateTimePicker.setShowTodayButton(true);
        dateTimePicker.setViewSelect(DateTimePickerView.DAY);
        dateTimePicker.reload();
    }

    @Override
    public void setValue(OffsetDateTime dateTime) {
        dateTimePicker.reload();
        dateTimePicker.setValue(GwtJavaTimeWorkaroundUtil.toDate(dateTime));
        timeZoneDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped(momentZoneId.getId() +
                " (" + momentZoneId.getZone().abbr(dateTime.toEpochSecond() * 1000) + ")").toSafeHtml().asString();
    }

    @Override
    public OffsetDateTime getValue() {
        LocalDateTime localDateTime = GwtJavaTimeWorkaroundUtil.toLocalDateTime(dateTimePicker.getValue());
        return OffsetDateTime.of(localDateTime,
                momentZoneId.getRules().getOffset(localDateTime));
    }

}
