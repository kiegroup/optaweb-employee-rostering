package org.optaweb.employeerostering.gwtui.client.common;

import elemental2.core.Date;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class MomentJs {

    @JsMethod(name = "moment", namespace = JsPackage.GLOBAL)
    public static native Moment moment(Date date, String format);

    @JsMethod(name = "moment", namespace = JsPackage.GLOBAL)
    public static native Moment moment(String date, String format);

    @JsMethod(name = "moment", namespace = JsPackage.GLOBAL)
    public static native Moment moment(Date date);

    @JsMethod(name = "moment", namespace = JsPackage.GLOBAL)
    public static native Moment moment(String date);

    @JsType(isNative = true, name = "Moment")
    public static class Moment {

        public native String format(String format);

        public native Moment day(double num);

        public native Date toDate();
    }
}
