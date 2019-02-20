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

package org.optaweb.employeerostering.webapp.tools;

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
