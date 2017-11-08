package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Provider;

import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Fetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

public class SpotNameFetchable implements Fetchable<List<String>> {
    Updatable<List<String>> updatable;
    Provider<Integer> tenantIdProvider;
    
    public SpotNameFetchable(Provider<Integer> tenantIdProvider) {
        this.tenantIdProvider = tenantIdProvider;
    }
    
    @Override
    public void fetchData(Command after) {
        SpotRestServiceBuilder.getSpotList(tenantIdProvider.get(), new FailureShownRestCallback<List<Spot>>() {
            @Override
            public void onSuccess(List<Spot> spotList) {
                updatable.onUpdate(spotList.stream().map((spot) -> spot.getName()).collect(Collectors.toList()));
                after.execute();
            }
        });
        
    }

    @Override
    public void setUpdatable(Updatable<List<String>> listener) {
        updatable = listener;
    }

}
