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

import java.util.Optional;

import com.google.gwtmockito.GwtMockitoTestRunner;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.optaweb.employeerostering.gwtui.client.popups.FormPopup;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class AbstractFormPopupTest {

    @Mock
    private HTMLDivElement root;

    @Mock
    private HTMLElement popupTitle;

    @Mock
    private HTMLButtonElement closeButton;

    @Mock
    private HTMLButtonElement cancelButton;

    @Mock
    private PopupFactory popupFactory;

    @Mock
    private FormPopup formPopup;

    @Mock
    private IsElement mockElement;

    private AbstractFormPopup testedAbstractFormPopup;

    @Before
    public void setUp() throws Exception {
        testedAbstractFormPopup = spy(new AbstractFormPopup(popupFactory, root, popupTitle, closeButton, cancelButton) {
            public void onClose() {
            }
        });
        when(popupFactory.getFormPopup(testedAbstractFormPopup)).thenReturn(Optional.of(formPopup));
    }

    @Test
    public void testShow() {
        testedAbstractFormPopup.show();
        verify(formPopup).show();
    }

    @Test
    public void testShowFor() {
        testedAbstractFormPopup.showFor(mockElement);
        verify(formPopup).showFor(mockElement);
    }

    @Test
    public void testHide() {
        testedAbstractFormPopup.show();
        testedAbstractFormPopup.hide();
        verify(formPopup).hide();
    }

    @Test
    public void testSetTitle() {
        final String TITLE_HTML = "<b>Hello World!</b>";
        testedAbstractFormPopup.setTitle(TITLE_HTML);
        assertThat(popupTitle.innerHTML).isEqualTo(TITLE_HTML);
    }

    @Test
    public void testOnCancelButtonClick() {
        MouseEvent mouseEvent = mock(MouseEvent.class);
        testedAbstractFormPopup.show();
        testedAbstractFormPopup.onCancelButtonClick(mouseEvent);
        verify(formPopup).hide();
        verify(mouseEvent).stopPropagation();
        verify(testedAbstractFormPopup).onClose();
    }

    @Test
    public void testOnCloseButtonClick() {
        MouseEvent mouseEvent = mock(MouseEvent.class);
        testedAbstractFormPopup.show();
        testedAbstractFormPopup.onCloseButtonClick(mouseEvent);
        verify(formPopup).hide();
        verify(mouseEvent).stopPropagation();
        verify(testedAbstractFormPopup).onClose();
    }
}
