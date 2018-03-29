package org.optaplanner.openshift.employeerostering.gwtui.client.pages.rotation;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.Assert;
import org.junit.Test;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;

public class Infinite60MinutesScaleTest {

    @Test
    public void test() {

        final OffsetDateTime start = OffsetDateTime.of(2018, 2, 7, 0, 0, 0, 0, ZoneOffset.UTC);
        final OffsetDateTime end = start.plusDays(7);
        final Long startInGridPixels = 0L;
        final Long endInGridPixels = 24L * 7L;

        final Infinite60MinutesScale scale = new Infinite60MinutesScale(start, end);

        // Boundaries
        Assert.assertEquals((Long) startInGridPixels, scale.toGridPixels(start));
        Assert.assertEquals((Long) endInGridPixels, scale.toGridPixels(end));
        Assert.assertEquals(start, scale.toScaleUnits(startInGridPixels));
        Assert.assertEquals(end, scale.toScaleUnits(endInGridPixels));

        // Over-boundaries
        Assert.assertEquals((Long) (-1L), scale.toGridPixels(start.minus(60, MINUTES)));
        Assert.assertEquals((Long) (endInGridPixels + 1L), scale.toGridPixels(end.plus(60, MINUTES)));
        Assert.assertEquals(start.minusMinutes(60L), scale.toScaleUnits(startInGridPixels - 1L));
        Assert.assertEquals(end.plusMinutes(60L), scale.toScaleUnits(endInGridPixels + 1));

        // 3 Days and 4 Hours after
        final OffsetDateTime d = start.plus(3, DAYS).plus(4, HOURS);
        Assert.assertEquals((Long) (24L * 3 + 4L), scale.toGridPixels(d));
        Assert.assertEquals(d, scale.toScaleUnits(24L * 3 + 4L));
    }
}
