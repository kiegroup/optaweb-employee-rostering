/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.server.admin;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Optional;

import org.optaweb.employeerostering.shared.admin.DatabaseType;

public class SystemPropertiesRetriever {

    public static final String ZONE_ID_SYSTEM_PROPERTY = "optaweb.generator.timeZoneId";
    public static final String DATABASE_STARTUP_DATA_SYSTEM_PROPERTY = "optaweb.generator.databaseStartupData";

    public static ZoneId determineZoneId() {
        String zoneIdProperty = System.getProperty(ZONE_ID_SYSTEM_PROPERTY);
        if (zoneIdProperty != null) {
            try {
                return ZoneId.of(zoneIdProperty);
            } catch (DateTimeException e) {
                throw new IllegalStateException("The system property (" + ZONE_ID_SYSTEM_PROPERTY
                                                        + ") has an invalid value (" + zoneIdProperty + ").", e);
            }
        }
        return ZoneId.systemDefault();
    }

    public static Optional<DatabaseType> determineDatabaseStartupData() {
        String databaseStartupDataProperty = System.getProperty(DATABASE_STARTUP_DATA_SYSTEM_PROPERTY);
        if (databaseStartupDataProperty != null) {
            try {
                return Optional.of(DatabaseType.valueOf(databaseStartupDataProperty));
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("The system property (" + DATABASE_STARTUP_DATA_SYSTEM_PROPERTY
                                                        + ") has an invalid value (" + databaseStartupDataProperty + ").", e);
            }
        } else {
            return Optional.empty();
        }
    }

    private SystemPropertiesRetriever() {
    }
}
