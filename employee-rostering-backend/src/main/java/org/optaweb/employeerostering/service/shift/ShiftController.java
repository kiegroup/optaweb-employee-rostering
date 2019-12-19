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

package org.optaweb.employeerostering.service.shift;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;

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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/tenant/{tenantId}/shift")
@CrossOrigin
@Validated
@Api(tags = "Shift")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
        Assert.notNull(shiftService, "shiftService must not be null.");
    }

    @ApiOperation("Get a list of all shifts")
    @GetMapping("/")
    public ResponseEntity<List<ShiftView>> getShiftList(@PathVariable @Min(0) Integer tenantId) {
        return new ResponseEntity<>(shiftService.getShiftList(tenantId), HttpStatus.OK);
    }

    @ApiOperation("Get a shift by id")
    @GetMapping("/{id}")
    public ResponseEntity<ShiftView> getShift(@PathVariable @Min(0) Integer tenantId, @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(shiftService.getShift(tenantId, id), HttpStatus.OK);
    }

    @ApiOperation("Delete a shift")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteShift(@PathVariable @Min(0) Integer tenantId, @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(shiftService.deleteShift(tenantId, id), HttpStatus.OK);
    }

    @ApiOperation("Add a new shift")
    @PostMapping("/add")
    public ResponseEntity<ShiftView> createShift(@PathVariable @Min(0) Integer tenantId,
                                                 @RequestBody @Valid ShiftView shiftView) {
        return new ResponseEntity<>(shiftService.createShift(tenantId, shiftView), HttpStatus.OK);
    }

    @ApiOperation("Update a shift")
    @PutMapping("/update")
    public ResponseEntity<ShiftView> updateShift(@PathVariable @Min(0) Integer tenantId,
                                                 @RequestBody @Valid ShiftView shiftView) {
        return new ResponseEntity<>(shiftService.updateShift(tenantId, shiftView), HttpStatus.OK);
    }
}
