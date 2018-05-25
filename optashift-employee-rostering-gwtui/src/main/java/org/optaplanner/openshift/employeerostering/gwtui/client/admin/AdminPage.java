package org.optaplanner.openshift.employeerostering.gwtui.client.admin;

import java.util.Map;

import javax.inject.Inject;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.NotificationSystem;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.shared.admin.AdminRestServiceBuilder;

@Templated
public class AdminPage implements Page {

    @Inject
    @DataField("reset-application-button")
    HTMLButtonElement resetApplicationButton;

    @Inject
    private LoadingSpinner loadingSpinner;

    @Inject
    private NotificationSystem notificationSystem;

    @EventHandler("reset-application-button")
    private void resetApplication(@ForEvent("click") MouseEvent e) {
        loadingSpinner.showFor("reset-application");
        AdminRestServiceBuilder.resetApplication(null, FailureShownRestCallback.onSuccess((success) -> {
            loadingSpinner.hideFor("reset-application");
            notificationSystem.notify("Application was reset successfully", "Application was reset successfully, please refresh the page.");
        }));
    }

    @Override
    public void restoreFromHistory(Map<String, String> params) {}
}
