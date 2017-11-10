package org.optaplanner.openshift.employeerostering.gwtui.client.roster;

import java.util.ArrayList;
import java.util.Collection;

public class Observable {
    Collection<Observer> observers;
    
    public Observable() {
        observers = new ArrayList<>();
    }
    
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    
    public void notifyObservers(Object arg) {
        observers.forEach((c) -> c.update(this, arg));
    }
}