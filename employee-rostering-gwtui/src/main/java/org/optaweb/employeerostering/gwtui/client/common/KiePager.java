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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import com.google.gwt.view.client.RowCountChangeEvent;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Span;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public class KiePager<T>
        implements
        Consumer<List<T>>,
        HasRows {

    public static final int DEFAULT_PAGER_LENGTH = 10;

    @DataField
    private Span currentRange;

    @DataField
    private Span rowCount;

    @DataField
    private Anchor previousPageButton;

    @DataField
    private Anchor nextPageButton;

    private SimplePager pager;

    private int startIndex;
    private int endIndex;

    private int pageSize;

    private Comparator<T> sorter;
    private ListComponent<T, ?> listPresenter;
    private List<T> listData;
    private Set<Handler> rangeChangeHandlers;
    private Set<com.google.gwt.view.client.RowCountChangeEvent.Handler> rowCountChangeHandlers;

    @Inject
    public KiePager(final Span currentRange,
                    final Span rowCount,
                    final Anchor previousPageButton,
                    final Anchor nextPageButton) {
        this.currentRange = currentRange;
        this.rowCount = rowCount;
        this.previousPageButton = previousPageButton;
        this.nextPageButton = nextPageButton;
    }

    @PostConstruct
    public void initKiePager() {
        sorter = (a, b) -> 0;
        pager = new SimplePager() {

            @Override
            // See https://stackoverflow.com/a/8015681
            public void setPageStart(int index) {
                if (getDisplay() != null) {
                    Range range = getDisplay().getVisibleRange();
                    int pageSize = range.getLength();

                    // Removed the min to show fixed ranges
                    //if (isRangeLimited && display.isRowCountExact()) {
                    //  index = Math.min(index, display.getRowCount() - pageSize);
                    //}

                    index = Math.max(0, index);
                    if (index != range.getStart()) {
                        getDisplay().setVisibleRange(index, pageSize);
                    }
                }
            }
        };
        this.pageSize = DEFAULT_PAGER_LENGTH;
        pager.setPageSize(pageSize);
        rangeChangeHandlers = new HashSet<>();
        rowCountChangeHandlers = new HashSet<>();
        this.listData = Collections.emptyList();
        pager.setDisplay(this);
    }

    public SimplePager getPager() {
        return pager;
    }

    public void setPresenter(ListComponent<T, ?> listPresenter) {
        this.listPresenter = listPresenter;
    }

    @EventHandler("previousPageButton")
    public void previous(ClickEvent e) {
        pager.previousPage();
        enableDisablePagerButtons();
    }

    @EventHandler("nextPageButton")
    public void next(ClickEvent e) {
        pager.nextPage();
        enableDisablePagerButtons();
    }

    private void enableDisablePagerButtons() {
        previousPageButton.setEnabled(pager.hasPreviousPage());
        nextPageButton.setEnabled(pager.hasNextPage());

        if (pager.hasPreviousPage()) {
            previousPageButton.getElement().getParentElement().setClassName("");
        } else {
            previousPageButton.getElement().getParentElement().setClassName("disabled");
        }

        if (pager.hasNextPage()) {
            nextPageButton.getElement().getParentElement().setClassName("");
        } else {
            nextPageButton.getElement().getParentElement().setClassName("disabled");
        }
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (event.getAssociatedType().equals(RowCountChangeEvent.getType())) {
            rowCountChangeHandlers.forEach((h) -> h.onRowCountChange((RowCountChangeEvent) event));
        } else if (event.getAssociatedType().equals(RangeChangeEvent.getType())) {
            rangeChangeHandlers.forEach((h) -> h.onRangeChange((RangeChangeEvent) event));
        }
    }

    @Override
    public HandlerRegistration addRangeChangeHandler(Handler handler) {
        rangeChangeHandlers.add(handler);
        return () -> rangeChangeHandlers.remove(handler);
    }

    @Override
    public HandlerRegistration addRowCountChangeHandler(
            com.google.gwt.view.client.RowCountChangeEvent.Handler handler) {
        rowCountChangeHandlers.add(handler);
        return () -> rowCountChangeHandlers.remove(handler);
    }

    @Override
    public int getRowCount() {
        return listData.size();
    }

    @Override
    public Range getVisibleRange() {
        return new Range(startIndex, pageSize);
    }

    @Override
    public boolean isRowCountExact() {
        return true;
    }

    @Override
    public void setRowCount(int count) {
        setRowCount(count, true);
    }

    @Override
    public void setRowCount(int count, boolean isExact) {
        rowCount.setText(count + "");
    }

    @Override
    public void setVisibleRange(int start, int length) {
        startIndex = start;
        endIndex = Math.min(start + length, listData.size());
        currentRange.setText((startIndex + 1) + "-" + endIndex);
        if (listPresenter != null && startIndex <= endIndex) {
            listPresenter.setValue(listData.subList(startIndex, endIndex));
        }
    }

    @Override
    public void setVisibleRange(Range range) {
        setVisibleRange(range.getStart(), range.getStart() + range.getLength());
    }

    public void refresh() {
        setVisibleRange(startIndex, Math.min(listData.size(), pageSize));
        setRowCount(listData.size());
    }

    public void sortBy(Comparator<T> comparator) {
        sorter = comparator;
        Collections.sort(listData, sorter);
        refresh();
    }

    @Override
    public void accept(List<T> data) {
        this.listData = data;
        Collections.sort(listData, sorter);
        pager.firstPage();
        enableDisablePagerButtons();
        RowCountChangeEvent.fire(this, listData.size(), true);
        refresh();
    }
}
