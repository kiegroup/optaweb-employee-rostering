package org.optaplanner.openshift.employeerostering.gwtui.client.common;

public interface OneWayMapping<F, T> {

    T map(F from);
}