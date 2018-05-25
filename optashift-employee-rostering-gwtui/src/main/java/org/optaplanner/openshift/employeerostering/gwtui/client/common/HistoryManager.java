package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.NavigationController.PageChange;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages;

@Singleton
public class HistoryManager {

    @Inject
    private Pages pages;
    @Inject
    private Event<PageChange> pageChangeEvent;

    @PostConstruct
    public void init() {
        History.addValueChangeHandler((historyToken) -> restoreFromHistory(historyToken.getValue()));
    };

    public void updateHistory(Pages.Id page, Map<String, String> params) {
        final StringBuilder builder = new StringBuilder();
        builder.append(URL.encodeQueryString(page.toString()));

        if (!params.isEmpty()) {
            builder.append("?");
            params.forEach((k, v) -> writeParameter(builder, k, v));
            builder.deleteCharAt(builder.length() - 1);
        }

        History.newItem(builder.toString(), false);
    }

    private void writeParameter(StringBuilder builder, String parameterName, String value) {
        builder.append(URL.encodeQueryString(parameterName));
        builder.append("=");
        builder.append(URL.encodeQueryString(value));
        builder.append("&");
    }

    public void restoreFromHistory(String historyToken) {
        Iterator<Character> historyStream = new Iterator<Character>() {

            int index = 0;
            char[] buf = historyToken.toCharArray();

            @Override
            public boolean hasNext() {
                return index < buf.length;
            }

            @Override
            public Character next() {
                index++;
                return buf[index - 1];
            }
        };
        try {
            Pages.Id page = parsePage(readUntil(historyStream, '?'));

            Map<String, String> parameters = new HashMap<>();
            while (historyStream.hasNext()) {
                String parameterName = readUntil(historyStream, '=');
                String parameterValue = readUntil(historyStream, '&');
                parameters.put(parameterName, parameterValue);
            }
            pageChangeEvent.fire(new PageChange(page, () -> {
                pages.get(page).restoreFromHistory(parameters);
            }));
        } catch (HistoryParsingException e) {
            // TODO: Display an error message
        }

    }

    private String readUntil(Iterator<Character> toRead, char delimiter) {
        StringBuilder readData = new StringBuilder();
        char currChar;
        while (toRead.hasNext() && (currChar = toRead.next()) != delimiter) {
            readData.append(currChar);
        }
        return URL.decodeQueryString(readData.toString());
    }

    private Pages.Id parsePage(String toParse) throws HistoryParsingException {
        try {
            return Pages.Id.valueOf(toParse);
        } catch (IllegalArgumentException e) {
            throw new HistoryParsingException("(" + toParse + ") is not a valid page.");
        }
    }

    private static class HistoryParsingException extends Exception {

        public HistoryParsingException(String msg) {
            super(msg);
        }

    }

}
