package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Fetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;

public class ConstantFetchable<I> implements Fetchable<I> {
    Updatable<I> updatable;
    I toReturn;
    
    public ConstantFetchable(I toReturn) {
        this.toReturn = toReturn;
    }
    @Override
    public void fetchData(Command after) {
        updatable.onUpdate(toReturn);
        after.execute();
    }

    @Override
    public void setUpdatable(Updatable<I> listener) {
        updatable = listener;
    }

}
