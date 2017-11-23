package org.optaplanner.openshift.employeerostering.shared.employee;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

@Entity
@NamedQueries({
        @NamedQuery(name = "EmployeeGroup.findByName",
                query = "select distinct g from EmployeeGroup g left join fetch g.employees" +
                        " where g.tenantId = :tenantId and g.name = :name" +
                        " order by g.name"),
        @NamedQuery(name = "EmployeeGroup.findAll",
                query = "select distinct g from EmployeeGroup g left join fetch g.employees" +
                        " where g.tenantId = :tenantId" +
                        " order by g.name"),
})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tenantId", "name"}))
public class EmployeeGroup extends AbstractPersistable {

    public static final Long ALL_GROUP_ID = Long.valueOf(-1);

    @NotNull
    @Size(min = 1, max = 120)
    private String name;

    //@JsonManagedReference
    @NotNull
    @ManyToMany
    private List<Employee> employees;

    @SuppressWarnings("unused")
    public EmployeeGroup() {
    }

    public EmployeeGroup(Integer tenantId, String name) {
        super(tenantId);
        this.name = name;
        employees = new ArrayList<>(2);
    }

    public boolean hasEmployee(Employee spot) {
        return employees.contains(spot);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(name);
        out.append(" [");
        for (Employee spot : employees) {
            out.append(spot.toString());
            out.append(',');
        }
        out.deleteCharAt(out.length() - 1);
        out.append(']');

        return out.toString();

    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public static EmployeeGroup getAllGroup(Integer tenantId) {
        EmployeeGroup out = new EmployeeGroup(tenantId, "ALL");
        out.id = ALL_GROUP_ID;
        return out;
    }

}
