package org.optaplanner.openshift.employeerostering.gwtui.client.resources.js;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface MomentBundle extends ClientBundle {

    MomentBundle INSTANCE = GWT.create(MomentBundle.class);

    @Source("moment.min.js")
    TextResource moment();

    @Source("moment-timezone-with-data.js")
    TextResource momentTimezoneWithData();
}
