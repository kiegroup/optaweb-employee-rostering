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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

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
        doReturn(Optional.of(formPopup)).when(popupFactory).getFormPopup(testedAbstractFormPopup);
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
