package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid;

import java.util.Collection;
import java.util.Collections;

public interface SingleGridObject<T, M> extends GridObject<T, M>, HasGridObjects<T, M> {

    default Collection<GridObject<T, M>> getGridObjects() {
        return Collections.singleton(this);
    }
}
