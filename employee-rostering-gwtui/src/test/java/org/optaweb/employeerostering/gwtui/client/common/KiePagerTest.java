/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Element;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Span;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class KiePagerTest {

    @Mock
    private Span currentRange;

    @Mock
    private Span rowCount;

    @Mock
    private Anchor previousPageButton;

    @Mock
    private Element previousPageButtonElement;

    @Mock
    private com.google.gwt.dom.client.Element previousPageButtonParentElement;

    @Mock
    private Anchor nextPageButton;

    @Mock
    private Element nextPageButtonElement;

    @Mock
    private com.google.gwt.dom.client.Element nextPageButtonParentElement;

    @Mock
    private ListComponent<Integer, ?> listPresenter;

    private KiePager<Integer> testedKiePager;

    @Before
    public void setUp() throws Exception {

        when(previousPageButton.getElement()).thenReturn(previousPageButtonElement);
        when(previousPageButtonElement.getParentElement()).thenReturn(previousPageButtonParentElement);

        when(nextPageButton.getElement()).thenReturn(nextPageButtonElement);
        when(nextPageButtonElement.getParentElement()).thenReturn(nextPageButtonParentElement);

        testedKiePager = spy(new KiePager<>(currentRange,
                                            rowCount,
                                            previousPageButton,
                                            nextPageButton));

        testedKiePager.initKiePager();
        testedKiePager.setPresenter(listPresenter);
    }

    @Test
    public void testInitKiePager() {
        final SimplePager pager = testedKiePager.getPager();
        assertThat(pager)
                .as("Pager should be initialized")
                .isNotNull();
    }

    @Test
    public void testSetStartIndexLogicOfSimplePager_InRange() {
        final SimplePager pager = testedKiePager.getPager();

        final int pageStartIndex = 5;
        pager.setPageStart(pageStartIndex);

        verify(testedKiePager).setVisibleRange(pageStartIndex, KiePager.DEFAULT_PAGER_LENGTH);
    }

    @Test
    public void testSetStartIndexLogicOfSimplePager_NegativeIndex() {
        final SimplePager pager = testedKiePager.getPager();

        final int pageStartIndex = -1;
        pager.setPageStart(pageStartIndex);

        verify(testedKiePager, never()).setVisibleRange(anyInt(), anyInt());
        assertThat(testedKiePager.getVisibleRange())
                .hasFieldOrPropertyWithValue("start", 0)
                .hasFieldOrPropertyWithValue("length", KiePager.DEFAULT_PAGER_LENGTH);
    }

    @Test
    public void testAcceptData_ItemsFitOnePage() {
        final List<Integer> data = Arrays.asList(3, 2, 1);

        testedKiePager.accept(data);

        verify(previousPageButton).setEnabled(false);
        verify(previousPageButtonParentElement).setClassName("disabled");

        verify(nextPageButton).setEnabled(false);
        verify(nextPageButtonParentElement).setClassName("disabled");

        verify(testedKiePager).setVisibleRange(eq(0), eq(data.size()));
        verify(testedKiePager).setRowCount(eq(data.size()));

        verify(listPresenter, times(2)).setValue(eq(data));
    }

    @Test
    public void testAcceptData_ItemsDoNotFitOnePage() {
        final List<Integer> data = Arrays.asList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0);

        testedKiePager.accept(data);

        verify(previousPageButton).setEnabled(false);
        verify(previousPageButtonParentElement).setClassName("disabled");

        verify(nextPageButton).setEnabled(true);
        verify(nextPageButtonParentElement).setClassName("");

        verify(testedKiePager, times(2)).setVisibleRange(0, KiePager.DEFAULT_PAGER_LENGTH);
        verify(testedKiePager).setRowCount(eq(data.size()));

        verify(listPresenter, times(2)).setValue(eq(data.subList(0, 10)));
    }

    @Test
    public void testMove() {
        final List<Integer> data = Arrays.asList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0);

        testedKiePager.accept(data);
        testedKiePager.next(mock(ClickEvent.class));

        verify(previousPageButton).setEnabled(true);
        verify(previousPageButtonParentElement).setClassName("");

        verify(nextPageButton).setEnabled(false);
        verify(nextPageButtonParentElement).setClassName("disabled");

        verify(testedKiePager).setVisibleRange(10, KiePager.DEFAULT_PAGER_LENGTH);
        verify(testedKiePager).setRowCount(eq(data.size()));

        verify(listPresenter).setValue(eq(data.subList(10, 11)));
    }

    @Test
    public void testAcceptData_Sorted() {
        final List<Integer> data = Arrays.asList(10, 6, 9);

        testedKiePager.sortBy(Comparator.comparingInt(a -> a));
        testedKiePager.accept(data);

        verify(listPresenter, times(2)).setValue(eq(Arrays.asList(6, 9, 10)));
    }

    @Test
    public void testRowCountChange() {
        final RowCountChangeEvent.Handler handler = mock(RowCountChangeEvent.Handler.class);
        final RowCountChangeEvent event = mock(RowCountChangeEvent.class);
        when(event.getAssociatedType()).thenReturn(RowCountChangeEvent.getType());

        testedKiePager.addRowCountChangeHandler(handler);
        testedKiePager.fireEvent(event);

        verify(handler).onRowCountChange(eq(event));
    }
}
