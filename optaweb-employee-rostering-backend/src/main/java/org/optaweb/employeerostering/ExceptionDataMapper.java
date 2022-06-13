package org.optaweb.employeerostering;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.persistence.EntityNotFoundException;
import javax.persistence.RollbackException;
import javax.ws.rs.core.Response;

import org.optaweb.employeerostering.domain.exception.ConstraintViolatedException;
import org.optaweb.employeerostering.domain.exception.ServerSideExceptionInfo;
import org.optaweb.employeerostering.util.HierarchyTree;

public class ExceptionDataMapper {

    private final HierarchyTree<Class<? extends Throwable>, ExceptionData> exceptionHierarchyTree;

    public ExceptionDataMapper() {
        this.exceptionHierarchyTree = new HierarchyTree<>((a, b) -> {
            if (a.equals(b)) {
                return HierarchyTree.HierarchyRelationship.IS_THE_SAME_AS;
            } else if (a.isAssignableFrom(b)) {
                return HierarchyTree.HierarchyRelationship.IS_ABOVE;
            } else if (b.isAssignableFrom(a)) {
                return HierarchyTree.HierarchyRelationship.IS_BELOW;
            } else {
                return HierarchyTree.HierarchyRelationship.HAS_NO_DIRECT_RELATION;
            }
        });

        for (ExceptionData exceptionData : ExceptionData.values()) {
            exceptionHierarchyTree.putInHierarchy(exceptionData.getExceptionClass(), exceptionData);
        }
    }

    public ExceptionData getExceptionDataForExceptionClass(Class<? extends Throwable> clazz) {
        Optional<ExceptionData> exceptionData = exceptionHierarchyTree.getHierarchyClassValue(clazz);
        if (exceptionData.isPresent()) {
            return exceptionData.get();
        } else {
            throw new IllegalStateException("No ExceptionData for exception class (" + clazz + ").");
        }
    }

    public enum ExceptionData {
        GENERIC_EXCEPTION("ServerSideException.generic", Response.Status.INTERNAL_SERVER_ERROR, Throwable.class,
                t -> Collections.emptyList()),
        ENTITY_CONSTRAINT_VIOLATION("ServerSideException.entityConstraintViolation", Response.Status.BAD_REQUEST,
                ConstraintViolatedException.class,
                t -> ((ConstraintViolatedException) t).getI18nMessageParameters()),
        ILLEGAL_ARGUMENT("ServerSideException.illegalArgument", Response.Status.INTERNAL_SERVER_ERROR,
                IllegalArgumentException.class,
                t -> Collections.singletonList(t.getMessage())),
        NULL_POINTER("ServerSideException.nullPointer", Response.Status.INTERNAL_SERVER_ERROR,
                NullPointerException.class, t -> Collections.emptyList()),
        ENTITY_NOT_FOUND("ServerSideException.entityNotFound", Response.Status.NOT_FOUND,
                EntityNotFoundException.class,
                t -> Collections.singletonList(t.getMessage())),
        TRANSACTION_ROLLBACK("ServerSideException.rollback", Response.Status.CONFLICT, RollbackException.class,
                t -> Collections.emptyList());

        private String i18nKey;
        private Response.Status statusCode;
        private Class<? extends Throwable> exceptionClass;
        private Function<Throwable, List<String>> parameterMapping;

        ExceptionData(String i18nKey, Response.Status statusCode, Class<? extends Throwable> exceptionClass,
                Function<Throwable, List<String>> parameterMapping) {
            this.i18nKey = i18nKey;
            this.statusCode = statusCode;
            this.exceptionClass = exceptionClass;
            this.parameterMapping = parameterMapping;
        }

        public Class<? extends Throwable> getExceptionClass() {
            return exceptionClass;
        }

        public Response.Status getStatusCode() {
            return statusCode;
        }

        public String getI18nKey() {
            return i18nKey;
        }

        public ServerSideExceptionInfo getServerSideExceptionInfoFromException(Throwable exception) {
            return new ServerSideExceptionInfo(exception, i18nKey,
                    parameterMapping.apply(exception).toArray(new String[0]));
        }
    }
}
