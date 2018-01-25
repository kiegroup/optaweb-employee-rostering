package org.optaplanner.openshift.employeerostering.gwtui.client.validation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.validation.client.AbstractValidationMessageResolver;
import com.google.gwt.validation.client.UserValidationMessagesResolver;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.ValidationMessages;

public class ErraiValidationMessagesResolver extends AbstractValidationMessageResolver
        implements UserValidationMessagesResolver {

    public ErraiValidationMessagesResolver() {
        super(GWT.create(ValidationMessages.class));
    }
}