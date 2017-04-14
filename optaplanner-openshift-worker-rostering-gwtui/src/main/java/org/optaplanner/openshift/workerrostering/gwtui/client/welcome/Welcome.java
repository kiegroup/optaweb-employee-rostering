package org.optaplanner.openshift.workerrostering.gwtui.client.welcome;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class Welcome extends Composite {

    interface MyUiBinder extends UiBinder<Widget, Welcome> {}
    private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    ListBox listBox;

    public Welcome(String... names) {
        // sets listBox
        initWidget(uiBinder.createAndBindUi(this));
        for (String name : names) {
            listBox.addItem(name);
        }
    }

}
