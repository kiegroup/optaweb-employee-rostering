package org.optaweb.employeerostering;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class OptaWebExceptionHandler implements ExceptionMapper<Throwable> {
    private final ExceptionDataMapper exceptionDataMapper;

    public OptaWebExceptionHandler() {
        this.exceptionDataMapper = new ExceptionDataMapper();
    }

    @Override
    public Response toResponse(Throwable e) {
        final ExceptionDataMapper.ExceptionData exceptionData =
                exceptionDataMapper.getExceptionDataForExceptionClass(e.getClass());
        return Response.status(exceptionData.getStatusCode())
                .entity(exceptionData.getServerSideExceptionInfoFromException(e))
                .build();
    }
}
