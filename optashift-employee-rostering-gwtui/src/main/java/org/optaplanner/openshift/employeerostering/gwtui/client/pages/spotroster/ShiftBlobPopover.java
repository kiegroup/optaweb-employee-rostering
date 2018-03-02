/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.spotroster;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import elemental2.dom.CSSProperties.WidthUnionType;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import elemental2.dom.Node;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;

@Templated
@ApplicationScoped
public class ShiftBlobPopover implements IsElement {

    @Inject
    @DataField("root")
    private HTMLDivElement root;

    @Inject
    @DataField("blob-border")
    private HTMLDivElement blobBorder;

    @Inject
    @DataField("dialog")
    private HTMLDivElement dialog;

    private HTMLElement parent;

    public void showFor(final BlobView<?, ?> blobView) {

        final Double blobWidth = blobView.getElement().offsetWidth;

        final Integer offsetLeft = getOffset(blobView.getElement(), e -> e.offsetLeft);
        final Integer offsetTop = getOffset(blobView.getElement(), e -> e.offsetTop) - new Double(parent.offsetTop).intValue() + new Double(parent.offsetLeft).intValue();

        dialog.style.left = offsetLeft + blobWidth - 9 + "px";
        dialog.style.top = offsetTop - 19 + "px";

        blobBorder.style.left = offsetLeft - 19 + "px";
        blobBorder.style.top = offsetTop - 19 + "px";
        blobBorder.style.width = WidthUnionType.of(blobWidth + "px");

        getElement().classList.remove("hidden");
    }

    public void hide() {
        getElement().classList.add("hidden");
    }

    public Integer getOffset(final Node initial, final Function<HTMLElement, Double> offset) {
        if (initial.equals(parent)) {
            return offset.apply((HTMLElement) initial).intValue();
        } else {
            return offset.apply((HTMLElement) initial).intValue() + getOffset(initial.parentNode, offset);
        }
    }

    @EventHandler("root")
    public void onClick(@ForEvent("click") final MouseEvent e) {
        hide();
        e.stopPropagation();
    }

    public void init(final IsElement parent) {
        this.parent = parent.getElement();
    }
}
