package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.util.function.Consumer;

import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import elemental2.promise.Promise;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;

public class Lockable<T> {

    @Inject
    private PromiseUtils promiseUtils;

    private boolean isLocked = false;

    private T instance;

    /**
     * Waits for instance to be available, and acquire a Lock. The Lock is released when the promise is resolved or rejected.
     * @return A promise returning the Lockable instance
     */
    public Promise<T> acquire() {
        return promiseUtils.promise((res, rej) -> {
            Scheduler.get().scheduleFixedDelay(() -> {
                if (!isLocked) {
                    isLocked = true;
                    try {
                        res.onInvoke(instance);
                    } finally {
                        isLocked = false;
                    }
                    return false;
                }
                return true;
            }, 10);
        });
    }

    public boolean isLocked() {
        return isLocked;
    }

    /**
     * Consumes the instance if it is not locked and return true, else returns false
     * @return true iff the instance was consumed
     */
    public boolean acquireIfPossible(Consumer<T> consumer) {
        if (isLocked) {
            return false;
        } else {
            isLocked = true;
            try {
                consumer.accept(instance);
            } finally {
                isLocked = false;
            }
            return true;
        }
    }

    public void setInstance(T instance) {
        this.instance = instance;
    }
}
