package org.optaplanner.openshift.employeerostering.gwtui.client.popups;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.client.local.api.elemental2.IsElement;

@ApplicationScoped
public class PopupFactory {
    
    private static FormPopup formPopup = null;
    
    public Optional<FormPopup> getFormPopup(IsElement content) {
        if (formPopup == null || !formPopup.isShowing()) {
            formPopup = new FormPopup(content);
            return Optional.of(formPopup);
        }
        return Optional.empty();
    }
    
    public void showErrorPopup(String message) {
        ErrorPopup.show(message);
    }
}
