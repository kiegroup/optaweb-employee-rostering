package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class JQuery {

    @JsMethod(name = "$", namespace = JsPackage.GLOBAL)
    public static native JQuery get(HTMLElement element);

    public native JQuery children(String selector);

    public native JQuery css(String propertyName, String propertyValue);

    public native Object data(String owner);

    public native double width();

    public native double height();

    public native Offset offset();

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    public static class Offset {

        public double top;
        public double left;
    }
}
