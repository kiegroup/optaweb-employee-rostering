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

package org.optaweb.employeerostering.standalone;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@WebFilter(urlPatterns = "/*")
public class WebConfig extends HttpFilter {

    @ConfigProperty(name = "quarkus.swagger-ui.path")
    String swaggerPath;

    private boolean isBackendPath(String path) {
        return path.startsWith("/rest/") || // REST methods
                path.equals(swaggerPath) || // Swagger docs
                path.startsWith("/q/"); // Quarkus pages (Swagger docs redirect to here)
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        chain.doFilter(request, response);

        final String path = request.getRequestURI();
        if (!isBackendPath(path) && response.getStatus() != 200 && !response.isCommitted()) {
            try {
                response.setStatus(200);
                if (path.startsWith("/assets/") || path.startsWith("/static/")) {
                    request.getRequestDispatcher("/").forward(request, response);
                } else {
                    response.setContentType("text/html");
                    request.getRequestDispatcher("/index.html").forward(request, response);
                }
            } finally {
                response.getOutputStream().close();
            }
        }
    }
}
