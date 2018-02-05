package org.optaplanner.openshift.employeerostering.server.common;

import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.openshift.employeerostering.shared.jackson.ConstraintMatchMixin;
import org.optaplanner.openshift.employeerostering.shared.jackson.IndictmentMixin;

//@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public ObjectMapperContextResolver() {
        mapper = new ObjectMapper();
        mapper.addMixIn(Indictment.class, IndictmentMixin.class);
        mapper.addMixIn(ConstraintMatch.class, ConstraintMatchMixin.class);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

}