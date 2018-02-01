package org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview;

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
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.ShiftDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

/**
 * Paging system of {@link TwoDayView}.
 *
 * @param <G> Type of the group.
 * @param <I> Type of the shift.
 * @param <D> {@link TimeRowDrawable} used for drawing shifts.
 */
public class TwoDayViewPager<G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G, I>> implements
                            HasRows, HasData<Collection<D>> {

    private TwoDayViewPresenter<G, I, D> presenter;

    /**
     * Listeners for range change events.
     */
    private Set<Handler> rangeHandlerSet = new HashSet<>();
    /**
     * Listeners for range count change events.
     */
    private Set<com.google.gwt.view.client.RowCountChangeEvent.Handler> rowCountHandlerSet = new HashSet<>();

    /**
     * Mandatory not-null selectionModel for {@link HasData}.
     */
    private SelectionModel<? super Collection<D>> selectionModel;

    /**
     * Cached visible rows (also contains empty rows).
     */
    private List<Collection<D>> cachedVisibleItemList;

    /**
     * Cached rows (also contains empty rows).
     */
    private List<Collection<D>> allItemList;

    /**
     * Page being shown.
     */
    private int page;

    /**
     * The first visible row.
     */
    private int rangeStart;

    /**
     * One more than the last visible row. (rangeStart + length)
     */
    private int rangeEnd;

    public TwoDayViewPager(TwoDayViewPresenter<G, I, D> presenter) {
        this.presenter = presenter;
        page = 0;
        rangeStart = 0;
        rangeEnd = 10;
        selectionModel = new NoSelectionModel<Collection<? extends D>>((g) -> (g.isEmpty()) ? null : g.iterator().next()
                                                                                                      .getGroupId());
    }

    /**
     * Sets the page.
     * @param page The page to visit.
     */
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
        for (i = 0; i < presenter.getGroupList().size() - 1; i++) {
            G group = presenter.getGroupList().get(i);
            int startPos = presenter.getState().getGroupPosMap().get(group);
            int endPos = presenter.getState().getGroupEndPosMap().get(group);
            if (rangeStart <= startPos && endPos <= rangeEnd) {
                return group;
            }
        }
        return presenter.getGroupList().get(i);
    }

    /**
     * Returns all groups that can be seen on the current page.
     * @return
     */
    public Set<G> getVisibleGroupSet() {
        int index = 0;
        Set<G> drawnSpots = new HashSet<>();
        int groupIndex = presenter.getState().getGroupIndex(getFirstVisibleGroup());

        drawnSpots.add(presenter.getState().getGroupList().get(groupIndex));

        for (Collection<D> group : getVisibleItems()) {
            if (!group.isEmpty()) {
                index++;
            } else {
                index++;
                if (groupIndex < presenter.getState().getGroupList().size() && rangeStart + index > presenter.getState()
                                                                                                             .getGroupEndPosMap()
                                                                                                             .getOrDefault(presenter.getState().getGroupList().get(groupIndex),
                                                                                                                           rangeStart + index)) {
                    groupIndex++;
                    if (groupIndex < presenter.getState().getGroupList().size()) {
                        drawnSpots.add(presenter.getState().getGroupList().get(groupIndex));
                    }
                }
            }
        }

        return drawnSpots;
    }

    /**
     * Notifies the pager widget changes to the collection has occurred. 
     */
    public void notifyCollectionChange() {
        presenter.getView().updatePager();
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (event.getAssociatedType().equals(RowCountChangeEvent.getType())) {
            rowCountHandlerSet.forEach((h) -> h.onRowCountChange((RowCountChangeEvent) event));
        } else if (event.getAssociatedType().equals(RangeChangeEvent.getType())) {
            rangeHandlerSet.forEach((h) -> h.onRangeChange((RangeChangeEvent) event));
        }
    }

    @Override
    public HandlerRegistration addRangeChangeHandler(Handler handler) {
        rangeHandlerSet.add(handler);
        return new Registration<>(handler, rangeHandlerSet);
    }

    @Override
    public HandlerRegistration addRowCountChangeHandler(
                                                        com.google.gwt.view.client.RowCountChangeEvent.Handler handler) {
        rowCountHandlerSet.add(handler);
        return new Registration<>(handler, rowCountHandlerSet);
    }

    @Override
    public int getRowCount() {
        if (presenter.getState().isAllDirty()) {
            getItemList();
        }
        return allItemList.size();
    }

    @Override
    public Range getVisibleRange() {
        return new Range(rangeStart, rangeEnd - rangeStart);
    }

    @Override
    public boolean isRowCountExact() {
        return true;
    }

    @Override
    public void setRowCount(int count) {
        //Unimplemented; we control the rows
    }

    @Override
    public void setRowCount(int count, boolean isExact) {
        //Unimplemented; we control the rows
    }

    @Override
    public void setVisibleRange(int start, int length) {
        if (start == rangeStart && rangeEnd - rangeStart == length) {
            return;
        }
        presenter.getState().setVisibleDirty(true);
        rangeStart = start;
        rangeEnd = start + length;
        presenter.draw();
    }

    @Override
    public void setVisibleRange(Range range) {
        if (range.getStart() == rangeStart && rangeEnd - rangeStart == range.getLength()) {
            return;
        }
        presenter.getState().setVisibleDirty(true);
        rangeStart = range.getStart();
        rangeEnd = range.getStart() + range.getLength();
        presenter.draw();
    }

    @Override
    public HandlerRegistration addCellPreviewHandler(com.google.gwt.view.client.CellPreviewEvent.Handler<Collection<D>> handler) {
        return new Registration<com.google.gwt.view.client.CellPreviewEvent.Handler<Collection<ShiftDrawable>>>();
    }

    @Override
    public SelectionModel<? super Collection<D>> getSelectionModel() {
        return selectionModel;
    }

    @Override
    public Collection<D> getVisibleItem(int indexOnPage) {
        if (presenter.getState().isDirty()) {
            getVisibleItems();
        }
        return cachedVisibleItemList.get(indexOnPage);
    }

    @Override
    public int getVisibleItemCount() {
        if (presenter.getState().isDirty()) {
            getVisibleItems();
        }
        return cachedVisibleItemList.size();
    }

    @Override
    public Iterable<Collection<D>> getVisibleItems() {
        if (presenter.getState().isDirty()) {
            cachedVisibleItemList = IntStream.range(rangeStart, Math.min(presenter.getState().getTimeSlotTable()
                                                                                  .getNumberOfRows(), rangeEnd)).mapToObj((k) -> presenter.getState()
                                                                                                                                          .getTimeSlotTable()
                                                                                                                                          .getVisibleRow(k))
                                             .collect(
                                                      Collectors.toList());
            presenter.getState().setVisibleDirty(false);
        }
        return cachedVisibleItemList;
    }

    /**
     * Returns a list of all rows.
     * @return A list of all rows.
     */
    public List<Collection<D>> getItemList() {
        if (presenter.getState().isAllDirty()) {
            allItemList = IntStream.range(0, presenter.getState().getTimeSlotTable().getNumberOfRows()).mapToObj((
                                                                                                                  k) -> presenter.getState()
                                                                                                                                 .getTimeSlotTable().getRow(k)).collect(Collectors
                                                                                                                                                                                  .toList());
            presenter.getState().setAllDirty(false);
        }
        return allItemList;
    }

    @Override
    public void setRowData(int start, List<? extends Collection<D>> values) {

    }

    @Override
    public void setSelectionModel(SelectionModel<? super Collection<D>> selectionModel) {
        this.selectionModel = selectionModel;
    }

    @Override
    public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
        setVisibleRange(range);
    }

    /**
     * Returns the current page.
     * @return The current page.
     */
    public int getPage() {
        return page;
    }

    /**
     * Simple handler to handle removal of handlers. 
     * @param <T> Type of handler being registered.
     */
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
