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

package org.optaweb.employeerostering.shared.exception;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerSideException {

    private String i18nKey;
    private String exceptionMessage;
    private List<String> messageParameters;
    private String exceptionClass;
    private List<String> stackTrace;
    private ServerSideException exceptionCause;

    @SuppressWarnings("unused")
    public ServerSideException() {

    }

    public ServerSideException(String i18nKey, String exceptionMessage, List<String> messageParameters,
                               String exceptionClass, List<String> stackTrace, ServerSideException exceptionCause) {
        this.i18nKey = i18nKey;
        this.exceptionMessage = exceptionMessage;
        this.messageParameters = messageParameters;
        this.exceptionClass = exceptionClass;
        this.stackTrace = stackTrace;
        this.exceptionCause = exceptionCause;
    }

    public ServerSideException(Throwable exception, String i18nKey, String... messageParameters) {
        this.i18nKey = i18nKey;
        this.exceptionMessage = exception.getMessage();
        this.exceptionClass = exception.getClass().getName();
        this.stackTrace = Arrays.asList(exception.getStackTrace()).stream()
                .map(s -> s.toString()).collect(Collectors.toList());
        this.messageParameters = Arrays.asList(messageParameters);
        if (exception.getCause() != null) {
            this.exceptionCause = new ServerSideException(exception.getCause(), "");
        } else {
            this.exceptionCause = null;
        }
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public void setI18nKey(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public List<String> getMessageParameters() {
        return messageParameters;
    }

    public void setMessageParameters(List<String> messageParameters) {
        this.messageParameters = messageParameters;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public List<String> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<String> stackTrace) {
        this.stackTrace = stackTrace;
    }

    public ServerSideException getExceptionCause() {
        return exceptionCause;
    }

    public void setExceptionCause(ServerSideException exceptionCause) {
        this.exceptionCause = exceptionCause;
    }
}
