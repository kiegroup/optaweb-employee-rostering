package org.optaplanner.openshift.employeerostering.server.admin;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.transaction.Transactional;

import org.optaplanner.openshift.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaplanner.openshift.employeerostering.server.roster.RosterGenerator;
import org.optaplanner.openshift.employeerostering.shared.admin.AdminRestService;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.rotation.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantConfiguration;

public class AdminRestServiceImpl extends AbstractRestServiceImpl implements AdminRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private RosterGenerator rosterGenerator;

    @Override
    @Transactional
    public void resetApplication() {
        // IMPORTANT: Delete entries that has Many-to-One relations first,
        // otherwise we break referential integrity
        deleteAllEntities(Shift.class, EmployeeAvailability.class, ShiftTemplate.class,
                Employee.class, Spot.class, Skill.class,
                TenantConfiguration.class, RosterState.class, Tenant.class);
        rosterGenerator.setUpGeneratedData();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void deleteAllEntities(Class<?>... entityTypes) {
        for (Class entityType : entityTypes) {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaDelete query = builder.createCriteriaDelete(entityType);
            query.from(entityType);
            entityManager.createQuery(query).executeUpdate();
        }
    }

}
