package org.optaplanner.openshift.employeerostering.shared.spot;

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
        @NamedQuery(name = "SpotGroup.findByName",
                query = "select distinct g from SpotGroup g left join fetch g.spots" +
                        " where g.tenantId = :tenantId and g.name = :name" +
                        " order by g.name"),
        @NamedQuery(name = "SpotGroup.findAll",
                query = "select distinct g from SpotGroup g left join fetch g.spots" +
                        " where g.tenantId = :tenantId" +
                        " order by g.name"),
})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tenantId", "name"}))
public class SpotGroup extends AbstractPersistable {

    @NotNull
    @Size(min = 1, max = 120)
    private String name;

    //@JsonManagedReference
    @NotNull
    @ManyToMany
    private List<Spot> spots;

    @SuppressWarnings("unused")
    public SpotGroup() {
    }

    public SpotGroup(Integer tenantId, String name) {
        super(tenantId);
        this.name = name;
        spots = new ArrayList<>(2);
    }

    public boolean hasSpot(Spot spot) {
        return spots.contains(spot);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(name);
        out.append(" [");
        for (Spot spot : spots) {
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

    public List<Spot> getSpots() {
        return spots;
    }

    public void setSpots(List<Spot> spots) {
        this.spots = spots;
    }

}
