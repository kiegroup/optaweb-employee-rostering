/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.server.common.jpa;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

/**
 * This class is Hibernate specific, because JPA 2.1's @Converter currently
 * cannot handle 1 class mapping to 2 SQL columns.
 */
public class OffsetDateTimeHibernateType implements CompositeUserType {

    @Override
    public Class returnedClass() {
        return OffsetDateTime.class;
    }

    @Override
    public String[] getPropertyNames() {
        return new String[] {"dateTime", "zoneOffset"};
    }

    @Override
    public Type[] getPropertyTypes() {
        // Not sure if we should use LocalDateTimeType.INSTANCE instead of TIMESTAMP
        return new Type[]{StandardBasicTypes.TIMESTAMP, StandardBasicTypes.INTEGER};
    }

    @Override
    public Object getPropertyValue(Object o, int propertyIndex) {
        if (o == null) {
            return null;
        }
        OffsetDateTime offsetDateTime = (OffsetDateTime) o;
        switch (propertyIndex) {
            case 0:
                return Timestamp.valueOf(offsetDateTime.toLocalDateTime());
            case 1:
                return offsetDateTime.getOffset().getTotalSeconds();
            default:
                throw new IllegalArgumentException("The propertyIndex (" + propertyIndex
                        + ") must be 0 or 1.");
        }
    }

    @Override
    public OffsetDateTime nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner)
            throws SQLException {
        if (resultSet == null) {
            return null;
        }
        Timestamp timestamp = (Timestamp) StandardBasicTypes.TIMESTAMP.nullSafeGet(resultSet, names[0], session, owner);
        if (timestamp == null) {
            throw new IllegalStateException("The timestamp (" + timestamp + ") for an "
                    + OffsetDateTime.class.getSimpleName() + "cannot be null.");
        }
        LocalDateTime localDateTime = timestamp.toLocalDateTime();
        Integer zoneOffsetSeconds = (Integer) StandardBasicTypes.INTEGER.nullSafeGet(resultSet, names[1], session, owner);
        if (zoneOffsetSeconds == null) {
            throw new IllegalStateException("The zoneOffsetSeconds (" + zoneOffsetSeconds + ") for an "
                    + OffsetDateTime.class.getSimpleName() + "cannot be null.");
        }
        return OffsetDateTime.of(localDateTime, ZoneOffset.ofTotalSeconds(zoneOffsetSeconds));
    }

    @Override
    public void nullSafeSet(PreparedStatement statement, Object value, int parameterIndex, SessionImplementor session)
            throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, StandardBasicTypes.TIMESTAMP.sqlType());
            statement.setNull(parameterIndex, StandardBasicTypes.INTEGER.sqlType());
            return;
        }
        OffsetDateTime offsetDateTime = (OffsetDateTime) value;
        statement.setTimestamp(parameterIndex, Timestamp.valueOf(offsetDateTime.toLocalDateTime()));
        statement.setInt(parameterIndex, offsetDateTime.getOffset().getTotalSeconds());
    }

    // ************************************************************************
    // Mutable related methods
    // ************************************************************************

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object deepCopy(Object value) {
        return value; // OffsetDateTime is immutable
    }

    @Override
    public Object replace(Object original, Object target, SessionImplementor session, Object owner) {
        return original; // OffsetDateTime is immutable
    }

    @Override
    public void setPropertyValue(Object component, int property, Object value) {
        throw new UnsupportedOperationException("A OffsetDateTime is immutable.");
    }

    // ************************************************************************
    // Other methods
    // ************************************************************************

    @Override
    public boolean equals(Object a, Object b) {
        if (a == b) {
            return true;
        } else if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    @Override
    public int hashCode(Object o) {
        if (o == null) {
            return 0;
        }
        return o.hashCode();
    }

    @Override
    public Serializable disassemble(Object value, SessionImplementor session) {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, SessionImplementor session, Object owner) {
        return cached;
    }

}
