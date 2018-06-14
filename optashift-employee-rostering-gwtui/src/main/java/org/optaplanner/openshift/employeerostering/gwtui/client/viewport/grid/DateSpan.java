package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid;

import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLElement;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public class DateSpan implements IsElement {

    @Inject
    @DataField("icon")
    @Named("span")
    private HTMLElement icon;

    @Inject
    @DataField("date")
    @Named("span")
    private HTMLElement date;

    public HTMLElement getIcon() {
        return icon;
    }

    public HTMLElement getDate() {
        return date;
    }
}
