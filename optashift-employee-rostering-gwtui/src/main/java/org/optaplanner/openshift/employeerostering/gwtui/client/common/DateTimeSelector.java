package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.user.client.TakesValue;
import elemental2.core.Function;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.DateTimeUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.DateTimeUtils.MomentZoneId;
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;

@Templated
public class DateTimeSelector implements TakesValue<DateTimeRange> {

    @Inject
    @DataField("date-time-picker")
    private HTMLInputElement dateTimePicker;

    @Inject
    @DataField("time-zone-display")
    @Named("span")
    private HTMLElement timeZoneDisplay;

    @Inject
    private DateTimeUtils dateTimeUtils;

    private MomentZoneId momentZoneId;

    private DateTimeRange dateTimeRange;

    @PostConstruct
    private void init() {
        momentZoneId = dateTimeUtils.getTenantZoneId();

        DateRangeOptions options = new DateRangeOptions();
        options.timePicker = true;
        options.autoUpdateInput = true;
        options.startDate = toJsDate(LocalDateTime.now());
        options.endDate = toJsDate(LocalDateTime.now().plusDays(1));

        createDateRangePicker(dateTimePicker, options, getCallbackFunction());
    }

    @Override
    public void setValue(DateTimeRange dateTimeRange) {
        this.dateTimeRange = dateTimeRange;
        getDateRangePickerData(dateTimePicker).setStartDate(toJsDate(GwtJavaTimeWorkaroundUtil.toLocalDateTime(dateTimeRange.getStartDateTime())));
        getDateRangePickerData(dateTimePicker).setEndDate(toJsDate(GwtJavaTimeWorkaroundUtil.toLocalDateTime(dateTimeRange.getEndDateTime())));
    }

    @Override
    public DateTimeRange getValue() {
        return dateTimeRange;
    }

    private void onNewDateSelected(elemental2.core.Date startDateTime, elemental2.core.Date endDateTime, String label) {
        LocalDateTime localStartDateTime = toLocalDateTime(startDateTime);
        LocalDateTime localEndDateTime = toLocalDateTime(endDateTime);
        OffsetDateTime offsetStartDateTime = OffsetDateTime.of(localStartDateTime, momentZoneId.getRules().getOffset(localStartDateTime));
        OffsetDateTime offsetEndDateTime = OffsetDateTime.of(localEndDateTime, momentZoneId.getRules().getOffset(localEndDateTime));
        dateTimeRange = new DateTimeRange(offsetStartDateTime, offsetEndDateTime);
    }

    private static elemental2.core.Date toJsDate(LocalDateTime dateTime) {
        return new elemental2.core.Date(dateTime.getYear(), dateTime.getMonthValue(),
                dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(),
                dateTime.getSecond(), 0);
    }

    private static LocalDateTime toLocalDateTime(elemental2.core.Date dateTime) {
        return LocalDateTime.ofEpochSecond(Math.round(elemental2.core.Date.parse(dateTime)),
                0, ZoneOffset.UTC);
    }

    private native Function getCallbackFunction() /*-{
        var that = this;
        return function(start,end,label) {
            that.@org.optaplanner.openshift.employeerostering.gwtui.client.common.DateTimeSelector::onNewDateSelected(*)(start,end,label);
        };
    }-*/;
    
    private native void createDateRangePicker(HTMLInputElement input, DateRangeOptions options,
                                              Function onApply) /*-{
        $wnd.jQuery(input).daterangepicker(options, onApply);
    }-*/;

    private native DateRangePicker getDateRangePickerData(HTMLInputElement input) /*-{
        return $wnd.jQuery(input).data('daterangepicker');
    }-*/;
    
    @JsType(isNative = true, namespace = JsPackage.GLOBAL)
    public static class DateRangePicker {
        public native void setStartDate(elemental2.core.Date date);
        public native void setEndDate(elemental2.core.Date date);
    }

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    public static class DateRangeOptions {
        public elemental2.core.Date startDate;
        public elemental2.core.Date endDate;
        public elemental2.core.Date minDate;
        public elemental2.core.Date maxDate;
        public String opens;
        public String drops;
        public Boolean showDropdowns;
        public Boolean showWeekNumbers;
        public Boolean showISOWeekNumbers;
        public Boolean singleDatePicker;
        public Boolean timePicker;
        public Boolean timePicker24Hour;
        /**
         * In minutes
         */
        public double timePickerIncrement;
        public Boolean timePickerSeconds;

        // TODO: Create these config objects
        // Object ranges;
        // Object dateLimit;
        public LocaleOptions locale;
        public Boolean showCustomRangeLabel;
        public Boolean autoApply;
        public Boolean autoUpdateInput;
        public Boolean linkedCalendars;
    }
    
    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    public static class LocaleOptions {
        String format;
        String separator;
        String applyLabel;
        String cancelLabel;
        String fromLabel;
        String toLabel;
        String customRangeLabel;
        String weekLabel;
        String[] daysOfWeek;
        String[] monthNames;
        double firstDay;
    }
    
    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    public static class DateLimitOptions {
        // TODO
    }

}
