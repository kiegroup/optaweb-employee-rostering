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

package org.optaweb.employeerostering.gwtui.client.util;

import javax.inject.Singleton;

import com.google.gwt.core.client.GWT;
import elemental2.core.Error;
import elemental2.core.JsObject;
import elemental2.promise.Promise;
import elemental2.promise.Promise.CatchOnRejectedCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn;

@Singleton
public class PromiseUtils {

    public Promise<Void> resolve() {
        return Promise.resolve((Promise<Void>) null);
    }

    public <T> Promise<T> promise(PromiseExecutorCallbackFn<T> executor) {
        return manage(new Promise<>(executor));
    }

    public <T> Promise<T> manage(Promise<T> promise) {
        return promise.catch_(this.<T> getDefaultCatch());
    }

    public <V> CatchOnRejectedCallbackFn<? extends V> getDefaultCatch() {
        return (e) -> {
            if (e instanceof Throwable) {
                GWT.getUncaughtExceptionHandler().onUncaughtException((Throwable) e);
            } else if (e instanceof Error) {
                Error error = (Error) e;
                GWT.getUncaughtExceptionHandler().onUncaughtException(new Throwable(error.toString()));
            } else {
                if (null != e) {
                    JsObject error = new JsObject(e);
                    // Exceptions seems to have a reference to the actual exception stored in "__java$exception"
                    // Hopefully this is also true in production mode, else most stack traces will be useless
                    if (error.hasOwnProperty("__java$exception")) {
                        Object exception = JsObject.getOwnPropertyDescriptor(error, "__java$exception").value;
                        if (exception instanceof Throwable) {
                            GWT.getUncaughtExceptionHandler().onUncaughtException((Throwable) exception);
                        } else {
                            GWT.getUncaughtExceptionHandler().onUncaughtException(new Throwable(e.toString()));
                        }
                    }
                    // GWT Native exceptions (like NPE) seem to have stack and message variables, not __java$exception though
                    else if (error.hasOwnProperty("message")) {
                        String message = JsObject.getOwnPropertyDescriptor(error, "message").value.toString();
                        GWT.getUncaughtExceptionHandler().onUncaughtException(new Throwable(message));
                    } else {
                        GWT.getUncaughtExceptionHandler().onUncaughtException(new Throwable("An unknown exception occured inside a promise: " + e.toString()));
                    }
                }
            }
            return new Promise<>((res, rej) -> rej.onInvoke(e));
        };
    }
}
