/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.common;

import elemental2.promise.Promise.PromiseExecutorCallbackFn;

public final class ExternalPromiseCallback<T> implements PromiseExecutorCallbackFn<T> {

    private ResolveCallbackFn<T> onResolve;
    private RejectCallbackFn onReject;

    @Override
    public final void onInvoke(final ResolveCallbackFn<T> resolve, final RejectCallbackFn reject) {
        onResolve = resolve;
        onReject = reject;
    }

    public final void resolve(final T value) {
        onResolve.onInvoke(value);
    }

    public final void reject(final Object error) {
        onReject.onInvoke(error);
    }
}
