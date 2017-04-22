package org.optaplanner.openshift.employeerostering.gwtui.client;

import java.util.List;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.github.nmorel.gwtjackson.rest.api.RestRequestBuilder;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import org.optaplanner.openshift.employeerostering.domain.Employee;
import org.optaplanner.openshift.employeerostering.domain.EmployeeServiceBuilder;
import org.optaplanner.openshift.employeerostering.gwtui.client.welcome.Welcome;

public class OptaShiftRosteringEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        // Keep in sync with web.xml
        RestRequestBuilder.setDefaultApplicationPath("/rest");

//        Button b = new Button("Click me 3",
//                (ClickHandler) event -> Window.alert("OptaShiftRosteringEntryPoint, the new GWT stuff"));
//        RootPanel.get().add(b);


        EmployeeServiceBuilder.getEmployeeList(new RestCallback<List<Employee>>() {
            @Override
            public void onSuccess(List<Employee> employeeList) {
                Welcome welcome = new Welcome(employeeList.toArray(new Employee[]{}));
                Document.get().getBody().appendChild(welcome.getElement());
            }

            @Override
            public void onFailure(Throwable e) {
                Welcome welcome = new Welcome(new Employee("ERROR " + e.toString()));
                Document.get().getBody().appendChild(welcome.getElement());
            }
        });
    }

}
