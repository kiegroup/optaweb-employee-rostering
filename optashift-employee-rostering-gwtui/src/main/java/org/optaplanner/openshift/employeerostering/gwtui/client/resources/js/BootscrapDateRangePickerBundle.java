package org.optaplanner.openshift.employeerostering.gwtui.client.resources.js;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface BootscrapDateRangePickerBundle extends ClientBundle {

    BootscrapDateRangePickerBundle INSTANCE = GWT.create(BootscrapDateRangePickerBundle.class);

    @Source("bootstrap-daterangepicker.js")
    TextResource dateRangePicker();
}
