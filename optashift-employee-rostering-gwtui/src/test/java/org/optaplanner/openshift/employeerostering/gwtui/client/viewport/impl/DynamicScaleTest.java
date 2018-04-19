package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.Assert;
import org.junit.Test;

public class DynamicScaleTest {

    private static final Double ACCEPTED_ERROR = Double.MIN_VALUE;

    @Test
    public void test() {
        testScale(LocalDateTime.of(LocalDate.of(2005, 4, 4), LocalTime.MIDNIGHT), Duration.ofDays(14), Duration.ofHours(4));
        testScale(LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.MIDNIGHT), Duration.ofDays(7), Duration.ofHours(6));
        testScale(LocalDateTime.of(LocalDate.of(2002, 1, 2), LocalTime.MIDNIGHT), Duration.ofDays(21), Duration.ofMinutes(30));
    }

    private void testScale(LocalDateTime start, Duration scaleLength, Duration gridPixelLength) {
        final LocalDateTime end = start.plus(scaleLength);
        final Double startInGridPixels = 0d;
        final Double endInGridPixels = (scaleLength.getSeconds() / (double) gridPixelLength.getSeconds());
        final Double lengthPerHour = (Duration.ofHours(1).getSeconds() / (double) gridPixelLength.getSeconds());

        final DynamicScale scale = new DynamicScale(start, end, gridPixelLength);

        // Boundaries
        Assert.assertEquals(startInGridPixels, scale.toGridUnits(start), ACCEPTED_ERROR);
        Assert.assertEquals(endInGridPixels, scale.toGridUnits(end), ACCEPTED_ERROR);
        Assert.assertEquals(start, scale.toScaleUnits(startInGridPixels));
        Assert.assertEquals(end, scale.toScaleUnits(endInGridPixels));

        // Over-boundaries
        Assert.assertEquals(-lengthPerHour, scale.toGridUnits(start.minusMinutes(60)), ACCEPTED_ERROR);
        Assert.assertEquals(endInGridPixels + lengthPerHour, scale.toGridUnits(end.plusMinutes(60)), ACCEPTED_ERROR);
        Assert.assertEquals(start.minusMinutes(60), scale.toScaleUnits(startInGridPixels - lengthPerHour));
        Assert.assertEquals(end.plusMinutes(60), scale.toScaleUnits(endInGridPixels + lengthPerHour));

        // 3 Days and 4 Hours after
        final LocalDateTime d = start.plusDays(3).plusHours(4);
        Assert.assertEquals(24d * 3 * lengthPerHour + 4d * lengthPerHour, scale.toGridUnits(d), ACCEPTED_ERROR);
        Assert.assertEquals(d, scale.toScaleUnits(24d * 3 * lengthPerHour + 4d * lengthPerHour));
    }
}
