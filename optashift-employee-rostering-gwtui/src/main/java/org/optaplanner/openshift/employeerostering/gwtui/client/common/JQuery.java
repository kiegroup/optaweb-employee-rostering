package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class JQuery {

    @JsMethod(name = "$", namespace = JsPackage.GLOBAL)
    public static native JQuery get(HTMLElement element);

    public native double width();

    public native double height();
}
