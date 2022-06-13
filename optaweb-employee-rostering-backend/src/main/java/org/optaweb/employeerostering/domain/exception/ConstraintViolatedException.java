package org.optaweb.employeerostering.domain.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

public class ConstraintViolatedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Object invalidObject;
    private final Class<?> validatedClass;
    private final Set<ConstraintViolation<Object>> constraintViolations;

    public ConstraintViolatedException(Object invalidObject, Class<?> validatedClass,
            Set<ConstraintViolation<Object>> violationSet) {
        this.invalidObject = invalidObject;
        this.validatedClass = validatedClass;
        this.constraintViolations = violationSet;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(validatedClass.getSimpleName() + " " + invalidObject.toString() +
                " violates the following constraints:\n");
        for (ConstraintViolation<?> cv : constraintViolations) {
            sb.append(cv.getMessage());
            sb.append("\n");
        }

        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public List<String> getI18nMessageParameters() {
        List<String> out = new ArrayList<>(1 + constraintViolations.size());
        out.add(validatedClass.getSimpleName());

        for (ConstraintViolation<?> cv : constraintViolations) {
            out.add(cv.getMessage());
        }

        return out;
    }
}
