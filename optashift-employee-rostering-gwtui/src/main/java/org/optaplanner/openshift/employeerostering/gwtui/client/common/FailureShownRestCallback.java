package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

public abstract class FailureShownRestCallback<T> extends RestCallback<T> {

    @Override
    public void onError(Response response) {
        String message = "Error calling REST method with status (" + response.getStatusCode() + ": " + response.getStatusText() + ") and text (" + response.getText() + ").";
        Window.alert(message);
        throw new IllegalStateException(message);
    }

    @Override
    public void onFailure(Throwable throwable) {
        Window.alert("Failure calling REST method: " + throwable.getMessage());
        throw new IllegalStateException("Failure calling REST method.", throwable);
    }

}
