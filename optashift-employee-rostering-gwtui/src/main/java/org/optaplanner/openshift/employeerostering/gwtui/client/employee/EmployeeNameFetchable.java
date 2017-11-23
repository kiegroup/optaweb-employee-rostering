package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Provider;

import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Fetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;

public class EmployeeNameFetchable implements Fetchable<List<EmployeeId>> {

    Updatable<List<EmployeeId>> updatable;
    Provider<Integer> tenantIdProvider;

    public EmployeeNameFetchable(Provider<Integer> tenantIdProvider) {
        this.tenantIdProvider = tenantIdProvider;
    }

    @Override
    public void fetchData(Command after) {
        EmployeeRestServiceBuilder.getEmployeeList(tenantIdProvider.get(), new FailureShownRestCallback<List<
                Employee>>() {

            @Override
            public void onSuccess(List<Employee> employeeList) {
                updatable.onUpdate(employeeList.stream().map((employee) -> new EmployeeId(employee)).collect(Collectors
                        .toList()));
                after.execute();
            }
        });

    }

    @Override
    public void setUpdatable(Updatable<List<EmployeeId>> listener) {
        updatable = listener;
    }

}
