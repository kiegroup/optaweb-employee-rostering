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

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.optaweb.employeerostering.shared.exception.ServerSideExceptionInfo;

public class RESTException extends Exception {

    private final transient ServerSideExceptionInfo serverSideException;
    private final transient TranslationService translationService;

    public RESTException(ServerSideExceptionInfo serverSideException, TranslationService translationService) {
        super(getExceptionFrom(serverSideException));
        this.serverSideException = serverSideException;
        this.translationService = translationService;
    }

    protected static Exception getExceptionFrom(ServerSideExceptionInfo serverSideException) {
        Exception out;
        if (serverSideException.getExceptionCause() != null) {
            out = new ServerException(getExceptionFrom(serverSideException.getExceptionCause()), serverSideException.getExceptionClass() + ": " + serverSideException.getExceptionMessage());
        } else {
            out = new ServerException(serverSideException.getExceptionClass() + ": " + serverSideException.getExceptionMessage());
        }
        out.setStackTrace(extractStackTrace(serverSideException));
        return out;
    }

    protected static StackTraceElement[] extractStackTrace(ServerSideExceptionInfo serverSideException) {
        StackTraceElement[] stackTrace = new StackTraceElement[serverSideException.getStackTrace().size()];
        for (int i = 0; i < stackTrace.length; i++) {
            String stackTraceLine = serverSideException.getStackTrace().get(i);
            int fileNameLocation = stackTraceLine.indexOf('(');
            int lineNumberLocation = stackTraceLine.indexOf(':', fileNameLocation);
            int methodNameLocation = stackTraceLine.lastIndexOf('.', fileNameLocation);
            String className = stackTraceLine.substring(0, methodNameLocation);
            String methodName = stackTraceLine.substring(methodNameLocation + 1, fileNameLocation);

            if (lineNumberLocation != -1) {
                String fileName = stackTraceLine.substring(fileNameLocation + 1, lineNumberLocation);
                int lineNumber = Integer.parseInt(stackTraceLine.substring(lineNumberLocation + 1, stackTraceLine.length() - 1));
                stackTrace[i] = new StackTraceElement(className, methodName, fileName, lineNumber);
            } else {
                if (stackTraceLine.substring(fileNameLocation).equals("(Native Method)")) {
                    stackTrace[i] = new StackTraceElement(className, methodName, null, -2);
                } else if (stackTraceLine.substring(fileNameLocation).equals("(Unknown Source)")) {
                    stackTrace[i] = new StackTraceElement(className, methodName, null, -1);
                } else {
                    String fileName = stackTraceLine.substring(fileNameLocation + 1, stackTraceLine.length() - 1);
                    stackTrace[i] = new StackTraceElement(className, methodName, fileName, -1);
                }
            }
        }
        return stackTrace;
    }

    @Override
    public String getMessage() {
        return translationService.format(serverSideException.getI18nKey(),
                                         serverSideException.getMessageParameters().toArray());
    }

    private static class ServerException extends Exception {

        private final String message;

        public ServerException(Throwable cause, String message) {
            super(cause);
            this.message = message;
        }

        public ServerException(String message) {
            super();
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
