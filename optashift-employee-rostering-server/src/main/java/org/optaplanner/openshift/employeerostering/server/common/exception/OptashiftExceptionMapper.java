package org.optaplanner.openshift.employeerostering.server.common.exception;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class OptashiftExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        StringBuilder builder = new StringBuilder();
        builder.append(exception.getMessage());
        for (StackTraceElement trace : exception.getStackTrace()) {
            builder.append(trace.toString()).append('\n');
        }
        return Response.status(resolveStatus(exception))
                .type(MediaType.TEXT_PLAIN)
                .entity(builder.toString())
                .build();
    }

    private Status resolveStatus(final Exception exception) {
        if (exception instanceof EntityNotFoundException) {
            return Status.NOT_FOUND;
        } else {
            return Status.INTERNAL_SERVER_ERROR;
        }
    }
}
