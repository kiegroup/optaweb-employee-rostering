package org.optaplanner.openshift.employeerostering.gwtui.client;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestRequestBuilder;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.api.TransportError;
import org.jboss.errai.bus.client.api.TransportErrorHandler;
import org.jboss.errai.common.client.dom.Document;
import org.jboss.errai.common.client.logging.LoggingHandlerConfigurator;
import org.jboss.errai.common.client.logging.handlers.ErraiConsoleLogHandler;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.MenuPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;

@EntryPoint
@Bundle("resources/i18n/OptaShiftUIConstants.properties")
public class OptaShiftRosteringEntryPoint {

    static {
        // Keep in sync with web.xml
        RestRequestBuilder.setDefaultApplicationPath("/rest");
    }

    @Inject
    private Document document;

    @Inject
    private MenuPanel menuPanel;

    @PostConstruct
    public void onModuleLoad() {
        document.getBody().appendChild(menuPanel.getElement());
        
        LoggingHandlerConfigurator config = LoggingHandlerConfigurator.get();
        ErraiConsoleLogHandler handler = config.getHandler(ErraiConsoleLogHandler.class);
        Formatter oldFormatter = handler.getFormatter();
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord log) {
                if (log.getLevel().equals(Level.SEVERE)) {
                    ErrorPopup.show(log.getMessage());
                }
                return oldFormatter.format(log);
            }
            
        });
    }
}