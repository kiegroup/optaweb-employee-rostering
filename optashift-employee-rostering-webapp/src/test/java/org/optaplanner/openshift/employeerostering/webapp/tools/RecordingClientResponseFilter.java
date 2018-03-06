package org.optaplanner.openshift.employeerostering.webapp.tools;

import java.util.Iterator;
import java.util.Stack;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

/**
 * Keeps history of ClientResponseContext - representation of server's response the client received.
 **/
public class RecordingClientResponseFilter implements ClientResponseFilter,
                                                      Iterator<ClientResponseContext> {

    private final Stack<ClientResponseContext> clientContextHistory = new Stack<>();

    @Override
    public void filter(final ClientRequestContext clientRequestContext,
                       final ClientResponseContext clientResponseContext) {
        clientContextHistory.push(clientResponseContext);
    }

    @Override
    public boolean hasNext() {
        return !clientContextHistory.isEmpty();
    }

    /**
     * Retrieves the latest received ClientResponseContext instance
     * @return The latest received ClientResponseContext instance
     */
    @Override
    public ClientResponseContext next() {
        return clientContextHistory.pop();
    }

    public void clear() {
        clientContextHistory.clear();
    }
}
