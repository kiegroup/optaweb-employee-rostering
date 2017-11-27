package org.optaplanner.openshift.employeerostering.server.common;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;

import javax.persistence.EntityManager;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

public class AbstractRestServiceImpl {

    protected void validateTenantIdParameter(Integer tenantId, AbstractPersistable persistable) {
        if (!Objects.equals(persistable.getTenantId(), tenantId)) {
            throw new IllegalStateException("The tenantId (" + tenantId
                    + ") does not match the persistable (" + persistable + ")'s tenantId (" + persistable.getTenantId() + ").");
        }
    }

    @SuppressWarnings("all")
    /**
     * This method is a work-around for there being no way to overwrite a Persistent/Detached instance with a Transient instance.
     * It fetches the instance hibernate is using, then update the hibernate instance's fields by using the getter/setting methods
     * obtained via reflection. If the field is a Collection, we remove all elements not in the new value and flush
     * to update the one-to-many relationship.
     *  
     * @param entityManager The entity manager
     * @param newValue The new value
     * @param id The id 
     * @throws RuntimeException If there is a field without a properly named getter/setter method (getFieldName and setFieldName for fieldName), or
     * something terribly wrong happened
     */
    protected <T extends AbstractPersistable> void update(EntityManager entityManager, T newValue, Long id) {
        try {
            Class<? extends T> clazz = (Class<? extends T>) newValue.getClass();
            newValue.setId(id);
            T hibernateInstance = entityManager.find(clazz, id);
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                String fieldName = field.getName();
                String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                try {
                    Object newFieldValue = clazz.getMethod(getterName).invoke(newValue);

                    if (newFieldValue instanceof Collection) {

                        Collection hibernateCollection = (Collection) clazz.getMethod(getterName).invoke(hibernateInstance);
                        Collection newCollection = (Collection) newFieldValue;

                        hibernateCollection.removeIf((e) -> !newCollection.contains(e));
                        newCollection.removeIf((e) -> hibernateCollection.contains(e));
                        entityManager.flush();

                        for (Object ele : newCollection) {
                            hibernateCollection.add(ele);
                        }
                    } else {
                        clazz.getMethod(setterName, field.getType()).invoke(hibernateInstance, newFieldValue);
                        }
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Either the setter or getter for " + fieldName + " is not defined." + " Please ensure both " + getterName + "() and " + setterName + "(" + field.getType().getName() + ") are defined.", e);
                    }
                }
            entityManager.merge(hibernateInstance);
            }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        }

}
