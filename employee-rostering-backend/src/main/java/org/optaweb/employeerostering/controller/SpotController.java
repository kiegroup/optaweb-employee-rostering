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

import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.SpotDTO;
import org.optaweb.employeerostering.service.SpotService;
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
@RequestMapping("/rest/tenant/{tenantId}/spot")
public class SpotController {

    private final SpotService spotService;

    public SpotController(SpotService spotService) {
        this.spotService = spotService;
        Assert.notNull(spotService, "spotService must not be null.");
    }

    @GetMapping
    public ResponseEntity<List<Spot>> getSpotList(@PathVariable Integer tenantId) {
        return new ResponseEntity<>(spotService.getSpotList(tenantId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Spot> getSpot(@PathVariable Integer tenantId, @PathVariable Long id) {
        return new ResponseEntity<>(spotService.getSpot(tenantId, id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteSpot(@PathVariable Integer tenantId, @PathVariable Long id) {
        return new ResponseEntity<>(spotService.deleteSpot(tenantId, id), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Spot> createSpot(@PathVariable Integer tenantId, @RequestBody SpotDTO spotDTO) {
        Spot spot = new Spot(spotDTO.getTenantId(), spotDTO.getName(), spotDTO.getRequiredSkillSet());
        spot.setId(spotDTO.getId());
        spot.setVersion(spotDTO.getVersion());
        return new ResponseEntity<>(spotService.createSpot(tenantId, spot), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Spot> updateSpot(@PathVariable Integer tenantId, @RequestBody SpotDTO spotDTO) {
        Spot spot = new Spot(spotDTO.getTenantId(), spotDTO.getName(), spotDTO.getRequiredSkillSet());
        spot.setId(spotDTO.getId());
        spot.setVersion(spotDTO.getVersion());
        return new ResponseEntity<>(spotService.updateSpot(tenantId, spot), HttpStatus.OK);
    }
}
