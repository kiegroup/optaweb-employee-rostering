package org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.ShiftDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

public class TwoDayViewPager<G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G, I>> implements
        HasRows, HasData<
                Collection<D>> {

    private TwoDayViewPresenter<G, I, D> presenter;

    private Collection<Handler> rangeHandlers = new ArrayList<>();
    private Collection<com.google.gwt.view.client.RowCountChangeEvent.Handler> rowCountHandlers = new ArrayList<>();
    private SelectionModel<? super Collection<D>> selectionModel;

    private List<Collection<D>> cachedVisibleItems;
    private List<Collection<D>> allItems;
    private int page, rangeStart, rangeEnd;

    public TwoDayViewPager(TwoDayViewPresenter<G, I, D> presenter) {
        this.presenter = presenter;
        page = 0;
        rangeStart = 0;
        rangeEnd = 10;
        selectionModel = new NoSelectionModel<Collection<? extends D>>((g) -> (g.isEmpty()) ? null : g.iterator().next()
                .getGroupId());
    }

    public void setPage(int page) {
        if (this.page == page) {
            return;
        }
        this.page = page;
        presenter.setToolBox(null);
        presenter.getCalendar().forceUpdate();
        presenter.draw();
    }

    public G getFirstVisibleGroup() {
        int i;
        for (i = 0; i < presenter.getGroups().size() - 1; i++) {
            G group = presenter.getGroups().get(i);
            int startPos = presenter.getState().getGroupPos().get(group);
            int endPos = presenter.getState().getGroupEndPos().get(group);
            if (rangeStart <= startPos && endPos <= rangeEnd) {
                return group;
            }
        }
        return presenter.getGroups().get(i);
    }

    public Collection<G> getVisibleGroups() {
        int index = 0;
        Set<G> drawnSpots = new HashSet<>();
        int groupIndex = presenter.getState().getGroupIndex(getFirstVisibleGroup());

        drawnSpots.add(presenter.getState().getGroups().get(groupIndex));

        for (Collection<D> group : getVisibleItems()) {
            if (!group.isEmpty()) {
                index++;
            } else {
                index++;
                if (groupIndex < presenter.getState().getGroups().size() && rangeStart + index > presenter.getState()
                        .getGroupEndPos()
                        .getOrDefault(presenter.getState().getGroups().get(groupIndex),
                                rangeStart + index)) {
                    groupIndex++;
                    if (groupIndex < presenter.getState().getGroups().size()) {
                        drawnSpots.add(presenter.getState().getGroups().get(groupIndex));
                    }
                }
            }
        }

        return drawnSpots;
    }

    public void notifyCollectionChange() {
        presenter.getView().updatePager();
    }

    public void fireEvent(GwtEvent<?> event) {
        if (event.getAssociatedType().equals(RowCountChangeEvent.getType())) {
            rowCountHandlers.forEach((h) -> h.onRowCountChange((RowCountChangeEvent) event));
        } else if (event.getAssociatedType().equals(RangeChangeEvent.getType())) {
            rangeHandlers.forEach((h) -> h.onRangeChange((RangeChangeEvent) event));
        }
    }

    public HandlerRegistration addRangeChangeHandler(Handler handler) {
        rangeHandlers.add(handler);
        return new Registration<>(handler, rangeHandlers);
    }

    public HandlerRegistration addRowCountChangeHandler(
            com.google.gwt.view.client.RowCountChangeEvent.Handler handler) {
        rowCountHandlers.add(handler);
        return new Registration<>(handler, rowCountHandlers);
    }

    public int getRowCount() {
        if (presenter.getState().isAllDirty()) {
            getItems();
        }
        return allItems.size();
    }

    public Range getVisibleRange() {
        return new Range(rangeStart, rangeEnd - rangeStart);
    }

    public boolean isRowCountExact() {
        return true;
    }

    public void setRowCount(int count) {
        //Unimplemented; we control the rows
    }

    public void setRowCount(int count, boolean isExact) {
        //Unimplemented; we control the rows
    }

    public void setVisibleRange(int start, int length) {
        if (start == rangeStart && rangeEnd - rangeStart == length) {
            return;
        }
        presenter.getState().setVisibleDirty(true);
        rangeStart = start;
        rangeEnd = start + length;
        presenter.draw();
    }

    public void setVisibleRange(Range range) {
        if (range.getStart() == rangeStart && rangeEnd - rangeStart == range.getLength()) {
            return;
        }
        presenter.getState().setVisibleDirty(true);
        rangeStart = range.getStart();
        rangeEnd = range.getStart() + range.getLength();
        presenter.draw();
    }

    public HandlerRegistration addCellPreviewHandler(com.google.gwt.view.client.CellPreviewEvent.Handler<Collection<
            D>> handler) {
        return new Registration<com.google.gwt.view.client.CellPreviewEvent.Handler<Collection<ShiftDrawable>>>();
    }

    public SelectionModel<? super Collection<D>> getSelectionModel() {
        return selectionModel;
    }

    public Collection<D> getVisibleItem(int indexOnPage) {
        if (presenter.getState().isDirty()) {
            getVisibleItems();
        }
        return cachedVisibleItems.get(indexOnPage);
    }

    public int getVisibleItemCount() {
        if (presenter.getState().isDirty()) {
            getVisibleItems();
        }
        return cachedVisibleItems.size();
    }

    public Iterable<Collection<D>> getVisibleItems() {
        if (presenter.getState().isDirty()) {
            cachedVisibleItems = IntStream.range(rangeStart, Math.min(presenter.getState().getTimeSlotTable()
                    .getNumberOfRows(), rangeEnd)).mapToObj((k) -> presenter.getState()
                            .getTimeSlotTable()
                            .getVisibleRow(k))
                    .collect(
                            Collectors.toList());
            presenter.getState().setVisibleDirty(false);
        }
        return cachedVisibleItems;
    }

    public List<Collection<D>> getItems() {
        if (presenter.getState().isAllDirty()) {
            allItems = IntStream.range(0, presenter.getState().getTimeSlotTable().getNumberOfRows()).mapToObj((
                    k) -> presenter.getState()
                            .getTimeSlotTable().getRow(k)).collect(Collectors
                                    .toList());
            presenter.getState().setAllDirty(false);
        }
        return allItems;
    }

    public void setRowData(int start, List<? extends Collection<D>> values) {

    }

    public void setSelectionModel(SelectionModel<? super Collection<D>> selectionModel) {
        this.selectionModel = selectionModel;
    }

    public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
        setVisibleRange(range);
    }

    public int getPage() {
        return page;
    }

    private class Registration<T> implements HandlerRegistration {

        Collection<T> backingCollection;
        T handler;

        public Registration(T handler, Collection<T> backingCollection) {
            this.handler = handler;
            this.backingCollection = backingCollection;
        }

        public Registration() {
            backingCollection = new HashSet<>();
        }

        @Override
        public void removeHandler() {
            backingCollection.remove(handler);
        }
    }
}
