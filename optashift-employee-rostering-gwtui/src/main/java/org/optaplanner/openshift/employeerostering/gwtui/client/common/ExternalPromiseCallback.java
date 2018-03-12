package org.optaplanner.openshift.employeerostering.gwtui.client.common;

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
