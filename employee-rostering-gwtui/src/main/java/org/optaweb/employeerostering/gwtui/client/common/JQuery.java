/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.common;

import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class JQuery {

    @JsMethod(name = "$", namespace = JsPackage.GLOBAL)
    public static native JQuery get(HTMLElement element);

    @JsMethod(name = "$", namespace = JsPackage.GLOBAL)
    public static native JQuery select(String selector);

    public native JQuery children(String selector);

    public native JQuery css(String propertyName, String propertyValue);

    public native JQuery addClass(String className);

    public native Object data(String owner);

    public native double width();

    public native double height();

    public native Offset offset();

    public native void on(String event, EventHandler handler);

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    public static class Offset {

        public double top;
        public double left;
    }

    @JsFunction
    public interface EventHandler {

        public void handleEvent();
    }
}
