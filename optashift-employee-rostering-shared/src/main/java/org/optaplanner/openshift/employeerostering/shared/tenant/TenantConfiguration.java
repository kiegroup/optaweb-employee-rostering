package org.optaplanner.openshift.employeerostering.shared.tenant;

import java.time.DayOfWeek;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.optaplanner.core.api.domain.lookup.PlanningId;

@Entity
public class TenantConfiguration {

    @OneToOne(mappedBy = "configuration", fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = false)
    @JsonBackReference
    private Tenant tenant;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @PlanningId
    protected Integer id;
    @Version
    protected Long version;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = false)
    private TenantConfigurationView view;

    @SuppressWarnings("unused")
    public TenantConfiguration() {
        view = new TenantConfigurationView(null, 1, DayOfWeek.MONDAY);
    }

    public TenantConfiguration(Tenant tenant, Integer templateDuration, DayOfWeek weekStart) {
        this.tenant = tenant;
        view = new TenantConfigurationView(tenant.getId(), templateDuration, weekStart);
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
        view.setTenantId(tenant.getId());
    }

    public String toString() {
        return "{Week Start: " + view.getWeekStart().toString() +
                ", Template Duration: " + view.getTemplateDuration().toString() + "}";
    }

    public TenantConfigurationView getView() {
        return view;
    }

    public void setView(TenantConfigurationView view) {
        this.view = view;
    }

}
