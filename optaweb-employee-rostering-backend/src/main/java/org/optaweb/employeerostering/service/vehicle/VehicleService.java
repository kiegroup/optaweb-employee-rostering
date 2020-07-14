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

package org.optaweb.employeerostering.service.vehicle;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.EntityNotFoundException;

import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.vehicle.Vehicle;
import org.optaweb.employeerostering.domain.vehicle.VehicleAvailability;
import org.optaweb.employeerostering.domain.vehicle.view.VehicleAvailabilityView;
import org.optaweb.employeerostering.domain.vehicle.view.VehicleView;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.optaweb.employeerostering.service.roster.RosterStateRepository;
import org.optaweb.employeerostering.util.VehicleListXlsxFileIO;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService extends AbstractRestService {

    private final VehicleRepository vehicleRepository;

    private final VehicleAvailabilityRepository vehicleAvailabilityRepository;

    private final RosterStateRepository rosterStateRepository;

    private final VehicleListXlsxFileIO vehicleListXlsxFileIO;

    public VehicleService(VehicleRepository vehicleRepository,
                           VehicleAvailabilityRepository vehicleAvailabilityRepository,
                           RosterStateRepository rosterStateRepository,
                           VehicleListXlsxFileIO vehicleListXlsxFileIO) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleAvailabilityRepository = vehicleAvailabilityRepository;
        this.rosterStateRepository = rosterStateRepository;
        this.vehicleListXlsxFileIO = vehicleListXlsxFileIO;
    }

    // ************************************************************************
    // Employee
    // ************************************************************************

    public Vehicle convertFromVehicleView(Integer tenantId, VehicleView vehicleView) {
        validateTenantIdParameter(tenantId, vehicleView);
        Vehicle vehicle = new Vehicle(tenantId, vehicleView.getName(),
        		vehicleView.getSkillProficiencySet());
        vehicle.setId(vehicleView.getId());
        vehicle.setVersion(vehicleView.getVersion());
        return vehicle;
    }

    @Transactional
    public List<Vehicle> getVehicleList(Integer tenantId) {
        return vehicleRepository.findAllByTenantId(tenantId, PageRequest.of(0, Integer.MAX_VALUE));
    }

    @Transactional
    public Vehicle getVehicle(Integer tenantId, Long id) {
        Vehicle vehicle= vehicleRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No Vehicle entity found with ID (" + id + ")."));

        validateTenantIdParameter(tenantId, vehicle);
        return vehicle;
    }

    @Transactional
    public Boolean deleteVehicle(Integer tenantId, Long id) {
        Optional<Vehicle> vehicleOptional = vehicleRepository.findById(id);

        if (!vehicleOptional.isPresent()) {
            return false;
        }

        validateTenantIdParameter(tenantId, vehicleOptional.get());
        vehicleRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Vehicle createVehicle(Integer tenantId, VehicleView vehicleView) {
        Vehicle vehicle = convertFromVehicleView(tenantId, vehicleView);
        validateTenantIdParameter(tenantId, vehicle);

        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle updateVehicle(Integer tenantId, VehicleView vehicleView){
    	Vehicle newVehicle = convertFromVehicleView(tenantId, vehicleView);

    	Vehicle oldVehicle = vehicleRepository
                .findById(newVehicle.getId())
                .orElseThrow(() -> new EntityNotFoundException("Vehicle entity with ID (" + newVehicle.getId() +
                                                                       ") not found."));

        if (!oldVehicle.getTenantId().equals(newVehicle.getTenantId())) {
            throw new IllegalStateException("Employee entity with tenantId (" + oldVehicle.getTenantId()
                                                    + ") cannot change tenants.");
        }

        oldVehicle.setName(newVehicle.getName());
        oldVehicle.setSkillProficiencySet(newVehicle.getSkillProficiencySet());
        return vehicleRepository.save(oldVehicle);
    }

    @Transactional
    public List<Vehicle> importVehiclesFromExcel(Integer tenantId, InputStream excelInputStream) throws IOException {
        List<VehicleView> excelVehicleList = vehicleListXlsxFileIO
                .getVehicleListFromExcelFile(tenantId, excelInputStream);

        final Set<String> addedVehicleSet = new HashSet<>();
        excelVehicleList.stream().flatMap(vehicle -> {
            if (addedVehicleSet.contains(vehicle.getName().toLowerCase())) {
                // Duplicate Vehicle; already in the stream
                return Stream.empty();
            }
            // Add vehicle to the stream
            addedVehicleSet.add(vehicle.getName().toLowerCase());
            return Stream.of(vehicle);
        }).forEach(vehicle -> {
            Vehicle oldVehicle = vehicleRepository.findVehicleByName(tenantId, vehicle.getName());
            if (oldVehicle != null) {
                vehicle.setId(oldVehicle.getId());
                vehicle.setVersion(oldVehicle.getVersion());
                updateVehicle(tenantId, vehicle);
            } else {
                createVehicle(tenantId, vehicle);
            }
        });

        return getVehicleList(tenantId);
    }

    protected void validateTenantIdParameter(Integer tenantId, Vehicle vehicle) {
        super.validateTenantIdParameter(tenantId, vehicle);
        for (Skill skill : vehicle.getSkillProficiencySet()) {
            if (!Objects.equals(skill.getTenantId(), tenantId)) {
                throw new IllegalStateException("The tenantId (" + tenantId + ") does not match the skillProficiency ("
                                                        + skill + ")'s tenantId (" + skill.getTenantId() + ").");
            }
        }
    }

    // ************************************************************************
    // VehicleAvailability
    // ************************************************************************

    private VehicleAvailability convertFromVehicleAvailabilityView(Integer tenantId,
                                                                     VehicleAvailabilityView
                                                                             vehicleAvailabilityView) {
        validateTenantIdParameter(tenantId, vehicleAvailabilityView);

        Vehicle vehicle = vehicleRepository
                .findById(vehicleAvailabilityView.getVehicleId())
                .orElseThrow(() -> new EntityNotFoundException("Vehicle entity with ID (" +
                                                                       vehicleAvailabilityView.getVehicleId() +
                                                                       ") not found."));

        validateTenantIdParameter(tenantId, vehicle);

        RosterState rosterState = rosterStateRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("RosterState entity with tenantId (" +
                                                                       tenantId + ") not found."));

        VehicleAvailability vehicleAvailability =
                new VehicleAvailability(rosterState.getTimeZone(), vehicleAvailabilityView, vehicle);
        vehicleAvailability.setState(vehicleAvailabilityView.getState());
        return vehicleAvailability;
    }

    @Transactional
    public VehicleAvailabilityView getVehicleAvailability(Integer tenantId, Long id) {
        VehicleAvailability vehicleAvailability = vehicleAvailabilityRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No VehicleAvailability entity found with ID (" + id +
                                                                       ")."));

        validateTenantIdParameter(tenantId, vehicleAvailability);

        RosterState rosterState = rosterStateRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("No RosterState entity found with tenantId (" +
                                                                       tenantId + ")."));
        return new VehicleAvailabilityView(rosterState.getTimeZone(), vehicleAvailability);
    }

    @Transactional
    public VehicleAvailabilityView createVehicleAvailability(Integer tenantId,
                                                               VehicleAvailabilityView vehicleAvailabilityView) {
        VehicleAvailability vehicleAvailability = convertFromVehicleAvailabilityView(tenantId,
                                                                                        vehicleAvailabilityView);
        vehicleAvailabilityRepository.save(vehicleAvailability);

        RosterState rosterState = rosterStateRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("No RosterState entity found with tenantId (" +
                                                                       tenantId + ")."));
        return new VehicleAvailabilityView(rosterState.getTimeZone(), vehicleAvailability);
    }

    @Transactional
    public VehicleAvailabilityView updateVehicleAvailability(Integer tenantId,
                                                               VehicleAvailabilityView vehicleAvailabilityView) {
        VehicleAvailability newVehicleAvailability = convertFromVehicleAvailabilityView(tenantId,
                                                                                           vehicleAvailabilityView);

        VehicleAvailability oldVehicleAvailability = vehicleAvailabilityRepository
                .findById(newVehicleAvailability.getId())
                .orElseThrow(() -> new EntityNotFoundException("VehicleAvailability entity with ID (" +
                                                                       newVehicleAvailability.getId() +
                                                                       ") not found."));

        if (!oldVehicleAvailability.getTenantId().equals(newVehicleAvailability.getTenantId())) {
            throw new IllegalStateException("VehicleAvailability entity with tenantId (" +
                                                    newVehicleAvailability.getTenantId() +
                                                    ") cannot change tenants.");
        }

        oldVehicleAvailability.setVehicle(newVehicleAvailability.getVehicle());
        oldVehicleAvailability.setStartDateTime(newVehicleAvailability.getStartDateTime());
        oldVehicleAvailability.setEndDateTime(newVehicleAvailability.getEndDateTime());
        oldVehicleAvailability.setState(newVehicleAvailability.getState());

        // Flush to increase version number before we duplicate it to VehicleAvailableView
        VehicleAvailability updatedVehicleAvailability =
                vehicleAvailabilityRepository.saveAndFlush(oldVehicleAvailability);

        RosterState rosterState = rosterStateRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("No RosterState entity found with tenantId (" +
                                                                       tenantId + ")."));
        return new VehicleAvailabilityView(rosterState.getTimeZone(), updatedVehicleAvailability);
    }

    @Transactional
    public Boolean deleteVehicleAvailability(Integer tenantId, Long id) {
        Optional<VehicleAvailability> vehicleAvailabilityOptional = vehicleAvailabilityRepository.findById(id);

        if (!vehicleAvailabilityOptional.isPresent()) {
            return false;
        }

        validateTenantIdParameter(tenantId, vehicleAvailabilityOptional.get());
        vehicleAvailabilityRepository.deleteById(id);
        return true;
    }
}
