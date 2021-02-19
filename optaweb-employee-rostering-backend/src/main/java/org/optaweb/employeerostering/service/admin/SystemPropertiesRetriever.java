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

package org.optaweb.employeerostering.service.admin;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SystemPropertiesRetriever {

    public static final String ZONE_ID_SYSTEM_PROPERTY = "optaweb.generator.timeZoneId";
    public static final String INITIAL_DATA_PROPERTY = "optaweb.generator.initial.data";

    @ConfigProperty(name = ZONE_ID_SYSTEM_PROPERTY)
    Optional<String> zoneId;

    @ConfigProperty(name = INITIAL_DATA_PROPERTY, defaultValue = "DEMO_DATA")
    String demoData;

    public SystemPropertiesRetriever() {
        this(Optional.of("UTC"), "DEMO_DATA");
    }

    public SystemPropertiesRetriever(Optional<String> zoneId, String demoData) {
        this.zoneId = zoneId;
        this.demoData = (demoData != null) ? demoData : "DEMO_DATA";
    }

    public enum InitialData {
        EMPTY,
        DEMO_DATA // default
    }

    public ZoneId determineZoneId() {
        if (zoneId.isPresent()) {
            try {
                return ZoneId.of(zoneId.get());
            } catch (DateTimeException e) {
                throw new IllegalStateException("The system property (" + ZONE_ID_SYSTEM_PROPERTY
                        + ") has an invalid value (" + zoneId.get() + ").", e);
            }
        }
        return ZoneId.systemDefault();
    }

    public InitialData determineInitialData() {
        try {
            return InitialData.valueOf(demoData);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("The system property (" + INITIAL_DATA_PROPERTY
                    + ") has an invalid value (" + demoData + ").", e);
        }
    }

}
