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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
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
    private Set<Consumer<List<T>>> filterListenerSet;
    private Function<T, String> elementToStringMapping;

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

    public void setElementToStringMapping(Function<T, String> elementToStringMapping) {
        this.elementToStringMapping = elementToStringMapping;
    }

    public List<T> getFilteredList() {
        return listToFilter.stream().filter((e) -> {
            return elementToStringMapping.apply(e).toLowerCase().contains(searchText.value.toLowerCase());
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

    public void addFilterListener(Consumer<List<T>> listener) {
        filterListenerSet.add(listener);
    }

    public void removeFilterListener(Consumer<List<T>> listener) {
        filterListenerSet.remove(listener);
    }

    private void updateListeners() {
        List<T> filteredList = getFilteredList();
        filterListenerSet.forEach((l) -> l.accept(filteredList));
    }
}
