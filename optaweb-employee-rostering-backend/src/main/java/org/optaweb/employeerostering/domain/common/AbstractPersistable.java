package org.optaweb.employeerostering.domain.common;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.optaplanner.core.api.domain.lookup.PlanningId;

@MappedSuperclass
public abstract class AbstractPersistable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @PlanningId
    private Long id;

    @NotNull
    private Integer tenantId;

    @Version
    private Long version;

    @SuppressWarnings("unused")
    public AbstractPersistable() {
    }

    protected AbstractPersistable(Integer tenantId) {
        this(null, tenantId);
    }

    protected AbstractPersistable(Long id, Integer tenantId) {
        this.id = id;
        this.tenantId = tenantId;
    }

    protected AbstractPersistable(AbstractPersistable other) {
        this.id = other.id;
        this.tenantId = other.tenantId;
        this.version = other.version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        AbstractPersistable other = (AbstractPersistable) o;
        if (id == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!id.equals(other.getId())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return ((id == null) ? 0 : id.hashCode());
    }

    public String toString() {
        return "[" + getClass().getSimpleName() + "-" + id + "]";
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
