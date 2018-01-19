package org.optaplanner.openshift.employeerostering.gwtui.client.resources.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface CssResources extends ClientBundle {

    public CssResources INSTANCE = GWT.create(CssResources.class);

    interface ErrorPopupCss extends CssResource {

        String main();

        String panel();

        String glass();
    }

    interface PopupCss extends CssResource {

        String main();

        String submit();

        String cancel();

        String buttonGroup();

        String submitDiv();

        String panel();

        String glass();

        String singleValueTagInput();

        String textbox();
    }

    interface LoadingIconCss extends CssResource {

        String main();

        String spin();

        String panel();
    }

    interface CalendarCss extends CssResource {

        String main();

        String employeeShiftViewIndifferent();

        String employeeShiftViewDesired();

        String employeeShiftViewUnavailable();

        String employeeShiftViewUndesired();

        String spotShiftView();

        String verticalSlider();

        String sliderButton();
    }

    @Source("errorpopup.css")
    ErrorPopupCss errorpopup();

    @Source("popup.css")
    PopupCss popup();

    @Source("loadingicon.css")
    LoadingIconCss loadingIcon();

    @Source("calendar.css")
    CalendarCss calendar();
}