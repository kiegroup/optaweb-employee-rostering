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

package org.optaweb.employeerostering.service.rotation;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.optaweb.employeerostering.domain.rotation.view.ShiftTemplateView;
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
@RequestMapping("/rest/tenant/{tenantId}/rotation")
@CrossOrigin
@Validated
@Api(tags = "Rotation")
public class RotationController {

    private final RotationService rotationService;

    public RotationController(RotationService rotationService) {
        this.rotationService = rotationService;
        Assert.notNull(rotationService, "rotationService must not be null.");
    }

    @ApiOperation("Get a list of all shift templates")
    @GetMapping("/")
    public ResponseEntity<List<ShiftTemplateView>> getShiftTemplateList(@PathVariable @Min(0) Integer tenantId) {
        return new ResponseEntity<>(rotationService.getShiftTemplateList(tenantId), HttpStatus.OK);
    }

    @ApiOperation("Get a shift template by id")
    @GetMapping("/{id}")
    public ResponseEntity<ShiftTemplateView> getShiftTemplate(@PathVariable @Min(0) Integer tenantId,
                                                              @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(rotationService.getShiftTemplate(tenantId, id), HttpStatus.OK);
    }

    @ApiOperation("Delete a shift template")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteShiftTemplate(@PathVariable @Min(0) Integer tenantId,
                                                       @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(rotationService.deleteShiftTemplate(tenantId, id), HttpStatus.OK);
    }

    @ApiOperation("Add a new shift template")
    @PostMapping("/add")
    public ResponseEntity<ShiftTemplateView> createShiftTemplate(@PathVariable @Min(0) Integer tenantId,
                                                                 @RequestBody @Valid
                                                                         ShiftTemplateView shiftTemplateView) {
        return new ResponseEntity<>(rotationService.createShiftTemplate(tenantId, shiftTemplateView), HttpStatus.OK);
    }

    @ApiOperation("Update a shift template")
    @PutMapping("/update")
    public ResponseEntity<ShiftTemplateView> updateShiftTemplate(@PathVariable @Min(0) Integer tenantId,
                                                                 @RequestBody @Valid
                                                                         ShiftTemplateView shiftTemplateView) {
        return new ResponseEntity<>(rotationService.updateShiftTemplate(tenantId, shiftTemplateView), HttpStatus.OK);
    }
}
