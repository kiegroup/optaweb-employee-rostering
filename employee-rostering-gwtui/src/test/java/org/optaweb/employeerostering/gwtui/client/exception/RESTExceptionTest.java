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
package org.optaweb.employeerostering.gwtui.client.exception;

import org.junit.Test;
import org.optaweb.employeerostering.shared.exception.ServerSideExceptionInfo;

import static org.assertj.core.api.Assertions.assertThat;

public class RESTExceptionTest {

    @Test
    public void testGetExceptionFrom() {
        Exception exception1 = new Exception("Test");
        IllegalStateException exception2 = new IllegalStateException(exception1);
        IllegalArgumentException exception3 = new IllegalArgumentException(exception2);

        ServerSideExceptionInfo exceptionInfo = new ServerSideExceptionInfo(exception3, "ServerSideExceptionInfo.test");
        Exception testedException = RESTException.getExceptionFrom(exceptionInfo);

        assertThat(testedException.getMessage()).isEqualTo(exception3.getClass().getName() + ": " + exception3.getMessage());
        assertStackTraceEquals(exception3.getStackTrace(), testedException.getStackTrace());

        assertThat(testedException.getCause().getMessage()).isEqualTo(exception2.getClass().getName() + ": " + exception2.getMessage());
        assertStackTraceEquals(exception2.getStackTrace(), testedException.getCause().getStackTrace());

        assertThat(testedException.getCause().getCause().getMessage()).isEqualTo(exception1.getClass().getName() + ": " + exception1.getMessage());
        assertStackTraceEquals(exception1.getStackTrace(), testedException.getCause().getCause().getStackTrace());

        assertThat(testedException.getCause().getCause().getCause()).isNull();
    }

    @Test
    public void testExtractStackTrace() {
        Exception exception = new Exception();
        ServerSideExceptionInfo exceptionInfo = new ServerSideExceptionInfo(exception, "ServerSideExceptionInfo.test");

        StackTraceElement[] stackTrace = RESTException.extractStackTrace(exceptionInfo);

        assertStackTraceEquals(exception.getStackTrace(), stackTrace);
    }

    private void assertStackTraceEquals(StackTraceElement[] expected, StackTraceElement[] actual) {
        assertThat(actual.length).isEqualTo(expected.length);

        // Native methods mess up containsExactly
        for (int i = 0; i < expected.length; i++) {
            if (expected[i].isNativeMethod()) {
                assertThat(actual[i].getLineNumber()).isEqualTo(-2);
                assertThat(actual[i].getClassName()).isEqualTo(expected[i].getClassName());
                assertThat(actual[i].getMethodName()).isEqualTo(expected[i].getMethodName());
            } else {
                assertThat(actual[i]).isEqualTo(expected[i]);
            }
        }
    }
}
