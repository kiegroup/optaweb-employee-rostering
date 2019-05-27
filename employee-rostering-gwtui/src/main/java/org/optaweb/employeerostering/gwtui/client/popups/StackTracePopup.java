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

package org.optaweb.employeerostering.gwtui.client.popups;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.gwtbootstrap3.client.ui.html.Span;
import org.optaweb.employeerostering.gwtui.client.resources.css.CssResources;

public class StackTracePopup extends PopupPanel {
    private HandlerRegistration windowResizeHandler;

    private StackTracePopup(String msg) {
        super(false);

        CssResources.INSTANCE.errorpopup().ensureInjected();
        setStyleName(CssResources.INSTANCE.errorpopup().panel());
        setGlassStyleName(CssResources.INSTANCE.errorpopup().glass());
        setGlassEnabled(true);

        VerticalPanel vertPanel = new VerticalPanel();
        HorizontalPanel horizontalSubpanel = new HorizontalPanel();

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.setHeight("50vh");
        Span content = new Span(new SafeHtmlBuilder()
                .appendEscapedLines(msg)
                .toSafeHtml().asString());

        scrollPanel.add(content);
        scrollPanel.setWidth(Window.getClientWidth() / 2 + "px");
        windowResizeHandler = Window.addResizeHandler((e) -> {
            scrollPanel.setWidth(e.getWidth() / 2 + "px");
        });

        horizontalSubpanel.add(scrollPanel);
        vertPanel.add(horizontalSubpanel);

        horizontalSubpanel = new HorizontalPanel();
        horizontalSubpanel.add(new Span());
        Button button = new Button("Close");
        button.addClickHandler((e) -> {
            StackTracePopup.this.hide();
            StackTracePopup.this.windowResizeHandler.removeHandler();
        });
        horizontalSubpanel.add(button);

        vertPanel.add(horizontalSubpanel);
        setWidget(vertPanel);
    }

    public static void show(String msg) {
        final StackTracePopup popup = new StackTracePopup(msg);
        popup.center();
    }
}
