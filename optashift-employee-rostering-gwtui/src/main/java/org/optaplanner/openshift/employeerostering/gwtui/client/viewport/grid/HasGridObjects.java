package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid;

import java.util.Collection;

public interface HasGridObjects<T, M> {

    Long getId();

    Collection<GridObject<T, M>> getGridObjects();
}
