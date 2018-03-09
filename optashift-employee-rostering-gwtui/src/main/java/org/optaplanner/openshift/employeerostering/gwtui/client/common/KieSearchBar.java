package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import elemental2.dom.Event;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;

@Templated
public class KieSearchBar<T> {

    @Inject
    @DataField("search-button")
    private HTMLButtonElement searchButton;

    @Inject
    @DataField("clear-button")
    private HTMLButtonElement clearButton;

    @Inject
    @DataField("search-input")
    private HTMLInputElement searchText;

    private List<T> listToFilter;
    private Set<Updatable<List<T>>> filterListenerSet;
    private OneWayMapping<T, String> elementToStringMapping;

    @PostConstruct
    public void initWidget() {
        this.listToFilter = Collections.emptyList();
        this.filterListenerSet = new HashSet<>();
        this.elementToStringMapping = (e) -> "";
    }

    public void setListToFilter(List<T> listToFilter) {
        this.listToFilter = listToFilter;
        updateListeners();
    }

    public void setElementToStringMapping(OneWayMapping<T, String> elementToStringMapping) {
        this.elementToStringMapping = elementToStringMapping;
    }

    public List<T> getFilteredList() {
        return listToFilter.stream().filter((e) -> {
            return elementToStringMapping.map(e).toLowerCase().contains(searchText.value.toLowerCase());
        })
                           .collect(Collectors.toCollection(ArrayList::new));
    }

    @EventHandler("search-button")
    public void onSearchClick(final @ForEvent("click") MouseEvent e) {
        updateListeners();
    }

    @EventHandler("clear-button")
    public void onClearClick(final @ForEvent("click") MouseEvent e) {
        searchText.value = "";
        updateListeners();
    }

    @EventHandler("search-input")
    public void onFilterChange(final @ForEvent("input") Event e) {
        updateListeners();
    }

    public void addFilterListener(Updatable<List<T>> listener) {
        filterListenerSet.add(listener);
    }

    public void removeFilterListener(Updatable<List<T>> listener) {
        filterListenerSet.remove(listener);
    }

    private void updateListeners() {
        List<T> filteredList = getFilteredList();
        filterListenerSet.forEach((l) -> l.onUpdate(filteredList));
    }
}
