package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.rotation;

import javax.inject.Inject;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.ROTATION_INVALIDATE;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.ROTATION_SAVE;

@Templated
public class RotationToolbar implements IsElement {
    
    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;
    
    @Inject
    @DataField("save-button")
    private HTMLButtonElement saveButton;

    @Inject
    private EventManager eventManager;
    
    @EventHandler("refresh-button")
    public void onRefreshButtonClick(@ForEvent("click") MouseEvent e) {
        eventManager.fireEvent(ROTATION_INVALIDATE);
    }

    @EventHandler("save-button")
    public void onSaveButtonClick(@ForEvent("click") MouseEvent e) {
        eventManager.fireEvent(ROTATION_SAVE);
    }
}
