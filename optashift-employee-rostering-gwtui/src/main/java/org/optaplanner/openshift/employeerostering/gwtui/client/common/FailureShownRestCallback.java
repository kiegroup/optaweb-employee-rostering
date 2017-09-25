package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.google.gwt.http.client.Response;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;

public abstract class FailureShownRestCallback<T> extends RestCallback<T> {

    @Override
    public void onError(Response response) {
        String message = "Error calling REST method with status (" + response.getStatusCode() + ": " + response.getStatusText() + ") and text (" +
            response.getText() + ").";
        ErrorPopup.show(message);
        throw new IllegalStateException(message);
    }

    @Override
    public void onFailure(Throwable throwable) {
        ErrorPopup.show("Failure calling REST method: " + throwable.getMessage());
        throw new IllegalStateException("Failure calling REST method.", throwable);
    }

}
