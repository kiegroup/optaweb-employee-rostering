package org.optaplanner.openshift.employeerostering.shared.tenant;

import java.time.DayOfWeek;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

@Entity
public class TenantConfigurationView {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @PlanningId
    protected Integer id;
    @Version
    protected Long version;

    @Nullable
    protected Integer tenantId;

    @NotNull
    private Integer templateDuration;
    @NotNull
    private DayOfWeek weekStart;

    @SuppressWarnings("unused")
    public TenantConfigurationView() {

    }

    public TenantConfigurationView(Integer tenantId, Integer templateDuration,
            DayOfWeek weekStart) {
        this.tenantId = tenantId;
        this.templateDuration = templateDuration;
        this.weekStart = weekStart;
    }

    public Integer getTemplateDuration() {
        return templateDuration;
    }

    public void setTemplateDuration(Integer templateDuration) {
        this.templateDuration = templateDuration;
    }

    public DayOfWeek getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(DayOfWeek weekStart) {
        this.weekStart = weekStart;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return tenantId + " {Week Start: " + getWeekStart().toString() +
                ", Template Duration: " + getTemplateDuration().toString() + "}";
    }
}
