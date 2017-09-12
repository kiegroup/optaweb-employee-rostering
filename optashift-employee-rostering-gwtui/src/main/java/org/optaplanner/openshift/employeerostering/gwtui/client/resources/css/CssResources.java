package org.optaplanner.openshift.employeerostering.gwtui.client.resources.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface CssResources extends ClientBundle {
    public CssResources INSTANCE =  GWT.create(CssResources.class);
    
    interface ErrorPopupCss extends CssResource {
        String main();
        String panel();
    }
    
    @Source("errorpopup.css")
    ErrorPopupCss errorpopup();
}