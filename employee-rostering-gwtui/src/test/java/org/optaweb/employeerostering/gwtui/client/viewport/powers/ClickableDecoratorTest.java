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

package org.optaweb.employeerostering.gwtui.client.viewport.powers;

import java.util.function.Consumer;

import com.google.gwtmockito.GwtMockitoTestRunner;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.GridObject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class ClickableDecoratorTest {

    @Mock
    private GridObject<Object, Object> mockGridObject;

    @Mock
    private HTMLElement mockElement;

    @Mock
    private Consumer<MouseEvent> action;

    private ClickableDecorator<Object, Object> clickableDecorator;

    @Before
    public void setUp() throws Exception {
        clickableDecorator = spy(new ClickableDecorator<>());
        when(mockGridObject.getElement()).thenReturn(mockElement);
    }

    @Test
    public void testApplyFor() {
        ClickableDecorator<Object, Object> out = clickableDecorator.applyFor(mockGridObject);
        verify(mockGridObject, atLeastOnce()).getElement();

        // Sadly object::method lambdas are not equal, so need to settle for any()
        verify(mockElement).addEventListener(eq("mousedown"), any());
        verify(mockElement).addEventListener(eq("mousemove"), any());
        verify(mockElement).addEventListener(eq("click"), any());
        assertThat(out).isEqualTo(clickableDecorator);
    }

    @Test
    public void testSetOnClickAction() {
        ClickableDecorator<Object, Object> out = clickableDecorator.applyFor(mockGridObject).onClick(action);
        assertThat(out).isEqualTo(clickableDecorator);
    }

    private MouseEvent mouseEvent(double x, double y) {
        MouseEvent out = mock(MouseEvent.class);
        out.screenX = x;
        out.screenY = y;
        return out;
    }

    @Test
    public void testNoDragClick() {
        clickableDecorator.applyFor(mockGridObject).onClick(action);
        clickableDecorator.gridObjectMouseDownListener(mouseEvent(10, 10));

        MouseEvent click = mouseEvent(10, 10);
        clickableDecorator.gridObjectMouseClickListener(click);
        verify(action).accept(click);
    }

    @Test
    public void testSmallDragClick() {
        clickableDecorator.applyFor(mockGridObject).onClick(action);
        clickableDecorator.gridObjectMouseDownListener(mouseEvent(10, 10));
        clickableDecorator.gridObjectMouseMoveListener(mouseEvent(12, 12));

        MouseEvent click = mouseEvent(12, 12);
        clickableDecorator.gridObjectMouseClickListener(click);
        verify(action).accept(click);
    }

    @Test
    public void testLargeDragClick() {
        clickableDecorator.applyFor(mockGridObject).onClick(action);
        clickableDecorator.gridObjectMouseDownListener(mouseEvent(10, 10));
        clickableDecorator.gridObjectMouseMoveListener(mouseEvent(14, 14));
        clickableDecorator.gridObjectMouseMoveListener(mouseEvent(18, 18));
        clickableDecorator.gridObjectMouseMoveListener(mouseEvent(24, 24));

        MouseEvent click = mouseEvent(24, 24);
        clickableDecorator.gridObjectMouseClickListener(click);
        verify(action, never()).accept(click);
    }

    @Test
    public void testInPlaceDragClick() {
        clickableDecorator.applyFor(mockGridObject).onClick(action);
        clickableDecorator.gridObjectMouseDownListener(mouseEvent(10, 10));

        for (int i = 0; i < 30; i++) {
            clickableDecorator.gridObjectMouseMoveListener(mouseEvent(12, 12));
            clickableDecorator.gridObjectMouseMoveListener(mouseEvent(10, 10));
        }

        MouseEvent click = mouseEvent(10, 10);
        clickableDecorator.gridObjectMouseClickListener(click);
        verify(action, never()).accept(click);
    }
}
