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

package org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import elemental2.dom.CSSProperties.WidthUnionType;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;

@Templated
@ApplicationScoped
public class BlobPopover implements IsElement {

    private static final int NEGATIVE_MARGIN_DISPLACEMENT = 21;
    private static final int MARGIN_FROM_SELECTED_BLOB = 10;

    @Inject
    @DataField("root")
    private HTMLDivElement root;

    @Inject
    @DataField("blob-highlight-border")
    private HTMLDivElement blobHighlightBorder;

    @Inject
    @DataField("content")
    private HTMLDivElement contentContainer;

    private BlobPopoverContent content;

    private HTMLElement parent;

    public void init(final IsElement parent,
                     final BlobPopoverContent content) {

        this.parent = parent.getElement();
        this.content = content.withParent(this);
    }

    public void showFor(final BlobView<?, ?> blobView) {

        content.setBlobView(blobView);

        contentContainer.innerHTML = "";
        contentContainer.appendChild(content.getElement());

        getElement().style.visibility = "hidden";
        getElement().classList.remove("hidden");

        final HTMLElement blobElement = blobView.getElement();

        final Integer offsetLeft = getOffsetRelativeTo(parent, blobElement, e -> e.offsetLeft) + NEGATIVE_MARGIN_DISPLACEMENT;
        final Integer offsetTop = getOffsetRelativeTo(parent, blobElement, e -> e.offsetTop) + NEGATIVE_MARGIN_DISPLACEMENT;

        // Using style.bottom/right is not possible because the parent does not have a fixed height/width
        content.getElement().style.top = top(offsetTop, blobElement);
        content.getElement().style.left = left(offsetLeft, blobElement);

        blobHighlightBorder.style.left = px(offsetLeft);
        blobHighlightBorder.style.top = px(offsetTop);
        blobHighlightBorder.style.width = WidthUnionType.of(px(blobElement.offsetWidth));

        getElement().style.visibility = "";
    }

    private String top(final Integer offsetTop,
                       final HTMLElement blobElement) {

        final double popoverHeight = content.getElement().offsetHeight;

        if (offsetTop + popoverHeight > parent.offsetHeight + 50) {
            return px(offsetTop - popoverHeight + blobElement.offsetHeight);
        } else {
            return px(offsetTop);
        }
    }

    private String left(final Integer offsetLeft,
                        final HTMLElement blobElement) {

        final double popoverWidth = content.getElement().offsetWidth;

        if (offsetLeft + popoverWidth > parent.offsetWidth - 50) {
            return px(offsetLeft - popoverWidth - MARGIN_FROM_SELECTED_BLOB);
        } else {
            return px(offsetLeft + blobElement.offsetWidth + MARGIN_FROM_SELECTED_BLOB);
        }
    }

    @EventHandler("root")
    public void onClick(@ForEvent("click") final MouseEvent e) {
        hide();
        e.stopPropagation();
    }

    public void hide() {
        getElement().classList.add("hidden");
    }

    private Integer getOffsetRelativeTo(final HTMLElement parent,
                                        final HTMLElement element,
                                        final Function<HTMLElement, Double> offsetFn) {

        if (element.equals(parent)) {
            return 0;
        }

        return offsetFn.apply(element).intValue()
                + getOffsetRelativeTo(parent, (HTMLElement) element.offsetParent, offsetFn);
    }

    private String px(final Object object) {
        return object + "px";
    }
}
