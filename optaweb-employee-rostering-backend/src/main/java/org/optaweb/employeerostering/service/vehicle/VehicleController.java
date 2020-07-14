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
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.optaweb.employeerostering.domain.vehicle.Vehicle;
import org.optaweb.employeerostering.domain.vehicle.view.VehicleAvailabilityView;
import org.optaweb.employeerostering.domain.vehicle.view.VehicleView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/rest/tenant/{tenantId}/vehicle")
@CrossOrigin
@Validated
@Api(tags = "Vehicle")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
        Assert.notNull(vehicleService, "vehicleService must not be null.");
    }

    // ************************************************************************
    // Vehicle
    // ************************************************************************

    @ApiOperation("Get a list of all vehicle")
    @GetMapping("/")
    public ResponseEntity<List<Vehicle>> getVehicleList(@PathVariable @Min(0) Integer tenantId) {
        return new ResponseEntity<>(vehicleService.getVehicleList(tenantId), HttpStatus.OK);
    }

    @ApiOperation("Get an vehicle by id")
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicle(@PathVariable @Min(0) Integer tenantId, @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(vehicleService.getVehicle(tenantId, id), HttpStatus.OK);
    }

    @ApiOperation("Delete an vehicle")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteVehicle(@PathVariable @Min(0) Integer tenantId,
                                                  @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(vehicleService.deleteVehicle(tenantId, id), HttpStatus.OK);
    }

    @ApiOperation("Add a new vehicle")
    @PostMapping("/add")
    public ResponseEntity<Vehicle> createVehicle(@PathVariable @Min(0) Integer tenantId,
                                                   @RequestBody @Valid VehicleView vehicleView) {
        return new ResponseEntity<>(vehicleService.createVehicle(tenantId, vehicleView), HttpStatus.OK);
    }

    @ApiOperation("Import vehicles from an Excel file")
    @PostMapping("/import")
    public ResponseEntity<List<Vehicle>> addVehiclesFromExcelFile(@PathVariable @Min(0) Integer tenantId,
                                                                    @RequestParam("file") MultipartFile excelDataFile)
            throws IOException {

        return new ResponseEntity<>(vehicleService.importVehiclesFromExcel(tenantId, excelDataFile.getInputStream()),
                                    HttpStatus.OK);
    }

    @ApiOperation("Update an vehicle")
    @PostMapping("/update")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable @Min(0) Integer tenantId,
                                                   @RequestBody @Valid VehicleView vehicleView) {
        return new ResponseEntity<>(vehicleService.updateVehicle(tenantId, vehicleView), HttpStatus.OK);
    }

    // ************************************************************************
    // VehicleAvailability
    // ************************************************************************

    @ApiOperation("Get an vehicle availability by id")
    @GetMapping("/availability/{id}")
    public ResponseEntity<VehicleAvailabilityView> getVehicleAvailability(@PathVariable @Min(0) Integer tenantId,
                                                                            @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(vehicleService.getVehicleAvailability(tenantId, id), HttpStatus.OK);
    }

    @ApiOperation("Add a new vehicle availability")
    @PostMapping("/availability/add")
    public ResponseEntity<VehicleAvailabilityView> createVehicleAvailability(@PathVariable @Min(0) Integer tenantId,
                                                                               @RequestBody @Valid
                                                                                       VehicleAvailabilityView
                                                                                       vehicleAvailabilityView) {
        return new ResponseEntity<>(vehicleService.createVehicleAvailability(tenantId, vehicleAvailabilityView),
                                    HttpStatus.OK);
    }

    @ApiOperation("Update an vehicle availability")
    @PutMapping("/availability/update")
    public ResponseEntity<VehicleAvailabilityView> updateVehicleAvailability(@PathVariable @Min(0) Integer tenantId,
                                                                               @RequestBody @Valid
                                                                                       VehicleAvailabilityView
                                                                                       vehicleAvailabilityView) {
        return new ResponseEntity<>(vehicleService.updateVehicleAvailability(tenantId, vehicleAvailabilityView),
                                    HttpStatus.OK);
    }

    @ApiOperation("Delete an vehicle availability")
    @DeleteMapping("/availability/{id}")
    public ResponseEntity<Boolean> deleteVehicleAvailability(@PathVariable @Min(0) Integer tenantId,
                                                              @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(vehicleService.deleteVehicleAvailability(tenantId, id), HttpStatus.OK);
    }
}
