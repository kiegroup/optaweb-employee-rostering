package org.optaplanner.openshift.employeerostering.server.common;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

public class AbstractRestServiceImpl {

    protected void validateTenantIdParameter(Integer tenantId, AbstractPersistable persistable) {
        if (!Objects.equals(persistable.getTenantId(), tenantId)) {
            throw new IllegalStateException("The tenantId (" + tenantId + ") does not match the persistable (" + persistable + ")'s tenantId (" + persistable.getTenantId() + ").");
        }
    }

    protected List<Long> getTableCountAndVersionSum(Integer tenantId, EntityManager entityManager, Class<? extends AbstractPersistable> entityClass) {
        final String queryString = "SELECT COUNT(id), SUM(version) FROM " +
                escapeTableName(entityClass.getSimpleName()) + " row WHERE row.tenantId = :tenantId";
        entityManager.createQuery(queryString);
        Object[] result = (Object[]) entityManager.createQuery(queryString)
                .setParameter("tenantId", tenantId)
                .getSingleResult();
        Long currCount = (Long) result[0];
        Long currVersionSum = (Long) result[1];

        return Arrays.asList(currCount, currVersionSum);

    }

    // AFAIK, JPA doesn't provide a method to verify a name is valid, so I am using
    // this (https://docs.oracle.com/html/E13946_01/ejb3_langref.html#ejb3_langref_from_vars)
    // to check if a name is valid, and throw an exeception is it is not
    private String escapeTableName(String table) {
        if (table.isEmpty()) {
            throw new IllegalStateException("table name is empty");
        }
        if (!Character.isJavaIdentifierStart(table.charAt(0)) && table.charAt(0) != '?') {
            throw new IllegalStateException("table [" + table + "] starts with an illegal character");
        }
        for (char c : table.substring(1).toCharArray()) {
            if (!Character.isJavaIdentifierPart(c) || c == '?') {
                throw new IllegalStateException("table [" + table + "] contains an illegal character");
            }
        }

        switch (table.toUpperCase()) {
            case "SELECT":
            case "FROM":
            case "WHERE":
            case "UPDATE":
            case "DELETE":
            case "JOIN":
            case "OUTER":
            case "INNER":
            case "LEFT":
            case "GROUP":
            case "BY":
            case "HAVING":
            case "FETCH":
            case "DISTINCT":
            case "OBJECT":
            case "NULL":
            case "TRUE":
            case "FALSE":
            case "NOT":
            case "AND":
            case "OR":
            case "BETWEEN":
            case "LIKE":
            case "IN":
            case "AS":
            case "UNKNOWN":
            case "EMPTY":
            case "MEMBER":
            case "OF":
            case "IS":
            case "AVG":
            case "MAX":
            case "MIN":
            case "SUM":
            case "COUNT":
            case "ORDER":
            case "ASC":
            case "DESC":
            case "MOD":
            case "UPPER":
            case "LOWER":
            case "TRIM":
            case "POSITION":
            case "CHARACTER_LENGTH":
            case "CHAR_LENGTH":
            case "BIT_LENGTH":
            case "CURRENT_TIME":
            case "CURRENT_DATE":
            case "CURRENT_TIMESTAMP":
            case "NEW":
            case "EXISTS":
            case "ALL":
            case "ANY":
            case "SOME":
                throw new IllegalStateException("table [" + table + "] is a reserved keyword");
        }
        return table;
    }

}
