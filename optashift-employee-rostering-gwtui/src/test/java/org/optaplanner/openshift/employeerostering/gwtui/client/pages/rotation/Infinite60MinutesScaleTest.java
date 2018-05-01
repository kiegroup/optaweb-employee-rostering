package org.optaplanner.openshift.employeerostering.gwtui.client.pages.rotation;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;

public class Infinite60MinutesScaleTest {

    private static final Double ACCEPTED_ERROR = Double.MIN_VALUE;

    @Test
    public void test() {

        final LocalDateTime start = LocalDateTime.of(2018, 2, 7, 0, 0, 0, 0);
        final LocalDateTime end = start.plusDays(7);
        final Double startInGridPixels = 0d;
        final Double endInGridPixels = 24d * 7d;

        final Infinite60MinutesScale scale = new Infinite60MinutesScale(start, end);

        // Boundaries
        Assert.assertEquals(startInGridPixels, scale.toGridPixels(start), ACCEPTED_ERROR);
        Assert.assertEquals(endInGridPixels, scale.toGridPixels(end), ACCEPTED_ERROR);
        Assert.assertEquals(start, scale.toScaleUnits(startInGridPixels));
        Assert.assertEquals(end, scale.toScaleUnits(endInGridPixels));

        // Over-boundaries
        Assert.assertEquals(-1d, scale.toGridPixels(start.minus(60, MINUTES)), ACCEPTED_ERROR);
        Assert.assertEquals(endInGridPixels + 1d, scale.toGridPixels(end.plus(60, MINUTES)), ACCEPTED_ERROR);
        Assert.assertEquals(start.minusMinutes(60), scale.toScaleUnits(startInGridPixels - 1));
        Assert.assertEquals(end.plusMinutes(60), scale.toScaleUnits(endInGridPixels + 1));

        // 3 Days and 4 Hours after
        final LocalDateTime d = start.plus(3, DAYS).plus(4, HOURS);
        Assert.assertEquals(24d * 3 + 4d, scale.toGridPixels(d), ACCEPTED_ERROR);
        Assert.assertEquals(d, scale.toScaleUnits(24d * 3 + 4d));
    }
}
