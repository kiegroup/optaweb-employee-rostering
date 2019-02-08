package org.optaweb.employeerostering.gwtui.client.common;

import com.google.gwtmockito.GwtMockitoTestRunner;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(GwtMockitoTestRunner.class)
public class LocalTimePickerTest {

    @Mock
    private HTMLInputElement datePickerTextField;

    @Mock
    private HTMLDivElement datePickerElement;

    @InjectMocks
    private LocalTimePicker testedLocalTimePicker;

    @Before
    public void setUp() throws Exception {
        testedLocalTimePicker.init();
    }

    // Hard to Unit Test this due to how JS Interops work; since this is a wrapper
    // class for a Javascript Object, the class depends on Javascript (and hence,
    // static methods which we cannot mock).
    @Test
    public void testReportValidity() {
        boolean out = testedLocalTimePicker.reportValidity();
        verify(datePickerTextField).reportValidity();
        assertThat(out).isEqualTo(datePickerTextField.reportValidity());
    }
}
