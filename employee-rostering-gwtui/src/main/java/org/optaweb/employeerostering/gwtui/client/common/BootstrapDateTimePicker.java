package org.optaweb.employeerostering.gwtui.client.common;

import elemental2.core.Date;
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.optaweb.employeerostering.gwtui.client.common.MomentJs.Moment;

class BootstrapDateTimePicker extends JQuery {

    @JsMethod(name = "$", namespace = JsPackage.GLOBAL)
    public static native BootstrapDateTimePicker get(HTMLElement element);

    @JsMethod
    public native void datetimepicker(BootstrapDateTimePickerOptions options);

    @JsMethod
    public native Date val();

    @JsMethod
    public native void val(String date);

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    @SuppressWarnings("unused")
    public static class BootstrapDateTimePickerIcons {

        String today;
    }

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    @SuppressWarnings("unused")
    public static class BootstrapDateTimePickerData {

        @JsMethod
        public native void date(String date);

        @JsMethod
        public native void date(Date date);

        @JsMethod
        public native void date(Moment moment);
    }

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    @SuppressWarnings("unused")
    public static class BootstrapDateTimePickerOptions {

        String format;
        String toolbarPlacement;
        boolean inline;
        boolean sideBySide;
        boolean allowInputToggle;
        boolean showTodayButton;
        BootstrapDateTimePickerIcons icons;
    }
}
