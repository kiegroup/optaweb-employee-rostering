package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.util.function.Consumer;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;

public abstract class FailureShownRestCallback<T> extends RestCallback<T> {

    private Consumer<Response> onError = response -> {
        String message = "Error calling REST method with status (" + response.getStatusCode() + ": " + response.getStatusText() + ") and text (" +
                         response.getText() + ").";
        GWT.getUncaughtExceptionHandler().onUncaughtException(new IllegalStateException(message));
    };

    private Consumer<Throwable> onFailure = throwable -> {
        GWT.getUncaughtExceptionHandler().onUncaughtException(throwable);
    };

    @Override
    public void onError(final Response response) {
        onError.accept(response);
    }

    @Override
    public void onFailure(final Throwable throwable) {
        onFailure.accept(throwable);
    }

    public FailureShownRestCallback<T> onError(final Consumer<Response> onError) {
        this.onError = onError;
        return this;
    }

    public FailureShownRestCallback<T> onFailure(final Consumer<Throwable> onFailure) {
        this.onFailure = onFailure;
        return this;
    }

    public static <T> FailureShownRestCallback<T> onSuccess(final Consumer<T> onSuccess) {
        return new FailureShownRestCallback<T>() {

            @Override
            public void onSuccess(final T ret) {
                onSuccess.accept(ret);
            }
        };
    }
}
