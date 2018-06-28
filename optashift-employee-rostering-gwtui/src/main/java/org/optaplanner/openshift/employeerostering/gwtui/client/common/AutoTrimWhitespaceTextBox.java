package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import org.gwtbootstrap3.client.ui.TextBox;

public class AutoTrimWhitespaceTextBox extends TextBox {
    @Override
    public String getValue() {
        return super.getValue().trim();
    }
}
