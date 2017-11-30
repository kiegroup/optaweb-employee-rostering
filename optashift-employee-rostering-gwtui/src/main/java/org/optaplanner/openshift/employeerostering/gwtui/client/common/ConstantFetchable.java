package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Fetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;

public class ConstantFetchable<I> implements Fetchable<I> {

    Updatable<I> updatable;
    I toReturn;
    boolean ran = false;

    public ConstantFetchable(I toReturn) {
        this.toReturn = toReturn;
    }

    @Override
    public void fetchData(Command after) {
        if (!ran) {
            updatable.onUpdate(toReturn);
            ran = true;
        }
        after.execute();
    }

    @Override
    public void setUpdatable(Updatable<I> listener) {
        updatable = listener;
        ran = false;
    }

}
