package org.optaplanner.openshift.employeerostering.gwtui.client.validation;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

import com.google.gwt.i18n.server.GwtLocaleImpl;
import com.google.gwt.i18n.shared.GwtLocale;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;

public class ErraiMessageInterpolator implements MessageInterpolator {

    private TranslationService messages;

    public ErraiMessageInterpolator withTranslator(TranslationService translator) {
        messages = translator;
        return this;
    }

    //@Override
    public String interpolate(String message, Context context) {
        if (messages == null) {
            return message;
        }
        ErrorPopup.show("Info: interpolate(\"" + message + "\", context)");
        String resolvedMessage = message;
        String i18nKey = resolvedMessage.substring(1, resolvedMessage.length() - 1);
        String translation = messages.getTranslation(i18nKey);

        if (translation != null) {
            return translation;
        }
        return resolvedMessage;
    }

    //@Override
    public String interpolate(String messageTemplate, Context context, GwtLocale locale) {
        if (messages == null) {
            return messageTemplate;
        }
        ErrorPopup.show("Info: interpolate(\"" + messageTemplate + "\", context, " + locale.toString() + ")");
        String resolvedMessage = messageTemplate;
        String i18nKey = resolvedMessage.substring(1, resolvedMessage.length() - 1);
        String oldLocale = messages.getActiveLocale();

        TranslationService.setCurrentLocaleWithoutUpdate(locale.toString());
        String translation = messages.getTranslation(i18nKey);
        TranslationService.setCurrentLocaleWithoutUpdate(oldLocale);

        if (translation != null) {
            return translation;
        }
        return resolvedMessage;
    }

    //@Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        // TODO Auto-generated method stub
        return interpolate(messageTemplate, context);
    }
}
