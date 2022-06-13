package org.optaweb.employeerostering.domain.exception;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerSideExceptionInfo {

    private String i18nKey;
    private String exceptionMessage;
    private List<String> messageParameters;
    private String exceptionClass;
    private List<String> stackTrace;
    private ServerSideExceptionInfo exceptionCause;

    @SuppressWarnings("unused")
    public ServerSideExceptionInfo() {

    }

    public ServerSideExceptionInfo(Throwable exception, String i18nKey, String... messageParameters) {
        this.i18nKey = i18nKey;
        this.exceptionMessage = exception.getMessage();
        this.exceptionClass = exception.getClass().getName();
        this.stackTrace = Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString).collect(Collectors.toList());
        this.messageParameters = Arrays.asList(messageParameters);
        if (exception.getCause() != null) {
            this.exceptionCause = new ServerSideExceptionInfo(exception.getCause(), "");
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

    public ServerSideExceptionInfo getExceptionCause() {
        return exceptionCause;
    }

    public void setExceptionCause(ServerSideExceptionInfo exceptionCause) {
        this.exceptionCause = exceptionCause;
    }
}
