package org.optaplanner.openshift.employeerostering.gwtui.client.common;

public class Value<T> {

    private T value;

    public Value(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        this.value = newValue;
    }
}
