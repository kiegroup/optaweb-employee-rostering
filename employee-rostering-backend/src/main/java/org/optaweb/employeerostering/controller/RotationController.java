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

package org.optaweb.employeerostering.controller;

import java.util.List;

import org.optaweb.employeerostering.domain.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.service.RotationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
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
public class RotationController {

    private final RotationService rotationService;

    public RotationController(RotationService rotationService) {
        this.rotationService = rotationService;
        Assert.notNull(rotationService, "rotationService must not be null.");
    }

    @GetMapping
    public ResponseEntity<List<ShiftTemplateView>> getShiftTemplateList(@PathVariable Integer tenantId) {
        return new ResponseEntity<>(rotationService.getShiftTemplateList(tenantId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftTemplateView> getShiftTemplate(@PathVariable Integer tenantId, @PathVariable Long id) {
        return new ResponseEntity<>(rotationService.getShiftTemplate(tenantId, id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteShiftTemplate(@PathVariable Integer tenantId, @PathVariable Long id) {
        return new ResponseEntity<>(rotationService.deleteShiftTemplate(tenantId, id), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<ShiftTemplateView> createShiftTemplate(@PathVariable Integer tenantId,
                                                              @RequestBody ShiftTemplateView shiftTemplateView) {
        return new ResponseEntity<>(rotationService.createShiftTemplate(tenantId, shiftTemplateView), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<ShiftTemplateView> updateShiftTemplate(@PathVariable Integer tenantId,
                                                              @RequestBody ShiftTemplateView shiftTemplateView) {
        return new ResponseEntity<>(rotationService.updateShiftTemplate(tenantId, shiftTemplateView), HttpStatus.OK);
    }
}
