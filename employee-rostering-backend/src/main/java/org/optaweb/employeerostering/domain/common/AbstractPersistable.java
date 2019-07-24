/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
