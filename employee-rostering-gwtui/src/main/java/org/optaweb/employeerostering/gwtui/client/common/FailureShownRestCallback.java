/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.common;

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
