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
