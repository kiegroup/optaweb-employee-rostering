/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
