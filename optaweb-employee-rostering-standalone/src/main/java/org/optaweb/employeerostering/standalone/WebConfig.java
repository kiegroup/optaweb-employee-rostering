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
