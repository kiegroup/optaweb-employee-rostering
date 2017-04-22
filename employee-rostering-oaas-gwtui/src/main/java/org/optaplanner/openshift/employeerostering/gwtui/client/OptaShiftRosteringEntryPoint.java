package org.optaplanner.openshift.employeerostering.gwtui.client;

import java.util.List;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.github.nmorel.gwtjackson.rest.api.RestRequestBuilder;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import org.optaplanner.openshift.employeerostering.domain.Person;
import org.optaplanner.openshift.employeerostering.domain.PersonServiceBuilder;
import org.optaplanner.openshift.employeerostering.gwtui.client.welcome.Welcome;

public class OptaShiftRosteringEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        // Keep in sync with web.xml
        RestRequestBuilder.setDefaultApplicationPath("/rest");

//        Button b = new Button("Click me 3",
//                (ClickHandler) event -> Window.alert("OptaShiftRosteringEntryPoint, the new GWT stuff"));
//        RootPanel.get().add(b);


        PersonServiceBuilder.getPersonList(new RestCallback<List<Person>>() {
            @Override
            public void onSuccess(List<Person> personList) {
                Welcome welcome = new Welcome(personList.toArray(new Person[]{}));
                Document.get().getBody().appendChild(welcome.getElement());
            }

            @Override
            public void onFailure(Throwable e) {
                Welcome welcome = new Welcome(new Person("ERROR " + e.toString()));
                Document.get().getBody().appendChild(welcome.getElement());
            }
        });
    }

}
