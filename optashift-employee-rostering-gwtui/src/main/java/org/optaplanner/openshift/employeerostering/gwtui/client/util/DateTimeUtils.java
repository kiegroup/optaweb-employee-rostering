package org.optaplanner.openshift.employeerostering.gwtui.client.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;

@Singleton
public class DateTimeUtils {

    @Inject
    private TenantStore tenantStore;

    public static int SECONDS_PER_MINUTE = 60;
    public static int MINUTES_PER_HOUR = 60;
    public static int HOURS_PER_DAY = 24;

    // We don't have Temporal, so we kinda need to do every combination...
    public int daysBetween(LocalDate a, OffsetDateTime b) {
        return (int) (Duration.between(OffsetDateTime.of(a.atTime(LocalTime.MIDNIGHT), b.getOffset()), b).getSeconds() / (SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY));
    }

    public LocalTime getLocalTimeOf(OffsetDateTime dateTime) {
        return LocalTime.of(dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond(), dateTime.getNano());
    }

    public MomentZoneId getTenantZoneId() {
        // Using knowledge that we deserialize into MomentZoneId so cast is always safe
        return (MomentZoneId) tenantStore.getCurrentTenantConfiguration().getTimeZone();
    }

    public MomentZoneId getZoneIdForZoneName(final String zoneId) {
        Zone zone = getZoneForName(zoneId);
        return new MomentZoneId(zone);
    }

    private static native Zone getZoneForName(String zoneId) /*-{
        return $wnd.moment.tz.zone(zoneId);
    }-*/;

    private static native Zone[] getZones() /*-{
        var zoneNames = $wnd.moment.tz.names();
        var out = [];
        for (var i = 0; i < zoneNames.length; i++) {
            out.push($wnd.moment.tz.zone(zoneNames[i]));
        }
        return out;
    }-*/;
    
    @JsType(isNative = true, namespace = JsPackage.GLOBAL)
    public static class Zone {
        public String name;
        public String[] abbrs;
        public double[] untils;
        public double[] offsets;
        
        public native double parse(Date date);
        public native double utcOffset(double timestamp);
        public native String abbr(double timestamp);
    }

    public static class MomentZoneId extends ZoneId {
        private final Zone zone;
        private final ZoneRules zoneRules;
        
        public MomentZoneId(Zone zone) {
            this.zone = zone;
            this.zoneRules = new MomentZoneRules(zone);
        }

        @Override
        public String getId() {
            return zone.name;
        }

        @Override
        public ZoneRules getRules() {
            return zoneRules;
        }
        
        public Zone getZone() {
            return zone;
        }
    }
        
    private static class MomentZoneRules implements ZoneRules {
        private Zone zone;
        
        public MomentZoneRules(Zone zone) {
            this.zone = zone;
        }

        @Override
        public boolean isFixedOffset() {
            return zone.offsets.length == 1;
        }

        @Override
        public ZoneOffset getOffset(Instant instant) {
            return ZoneOffset.ofTotalSeconds((int) Math.round(zone.utcOffset(instant.getEpochSecond()) * 60));
        }

        @Override
        public ZoneOffset getOffset(LocalDateTime localDateTime) {
            return ZoneOffset.ofTotalSeconds((int) Math.round(zone.parse(GwtJavaTimeWorkaroundUtil.toDate(localDateTime)) * 60));
        }

        @Override
        public List<ZoneOffset> getValidOffsets(LocalDateTime localDateTime) {
            // moment-timezone doesn't provide a utility for this, so niether will we
            return Arrays.asList(getOffset(localDateTime));
        }

        @Override
        public ZoneOffset getStandardOffset(Instant instant) {
            return getOffset(instant);
        }

        @Override
        public Duration getDaylightSavings(Instant instant) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDaylightSavings(Instant instant) {
            throw new UnsupportedOperationException();
        }

        // default {
        // return (getStandardOffset(instant).equals(getOffset(instant)) == false);
        // }

        @Override
        public boolean isValidOffset(LocalDateTime localDateTime, ZoneOffset offset) {
            return getValidOffsets(localDateTime).contains(offset);
        }

        @Override
        public boolean equals(Object otherRules) {
            if (otherRules instanceof MomentZoneRules) {
                MomentZoneRules other = (MomentZoneRules) otherRules;
                return getZoneId().equals(other.getZoneId());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getZoneId().hashCode();
        }
        
        private String getZoneId() {
            return zone.name;
        }
    }

}
