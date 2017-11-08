package org.optaplanner.openshift.employeerostering.gwtui.client.interfaces;

public interface Fetchable<I> {
    final static Command DO_NOTHING = () -> {};
    
    void fetchData(Command after);
    void setUpdatable(Updatable<I> listener);
    
    interface Command {
        void execute();
    }
}
