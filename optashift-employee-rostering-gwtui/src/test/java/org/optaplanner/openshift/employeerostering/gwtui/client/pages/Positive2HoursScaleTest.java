package org.optaplanner.openshift.employeerostering.gwtui.client.pages;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.YEARS;

public class Positive2HoursScaleTest {

    private static final Double ACCEPTED_ERROR = Double.MIN_VALUE;

    @Test
    public void test() {

        final LocalDateTime start = LocalDateTime.of(2018, 2, 7, 0, 0, 0, 0);
        final LocalDateTime end = start.plusDays(7);

        final Positive2HoursScale scale = new Positive2HoursScale(start, end);

        // Boundaries
        Assert.assertEquals(0d, scale.toGridPixels(start), ACCEPTED_ERROR);
        Assert.assertEquals(84d, scale.toGridPixels(end), ACCEPTED_ERROR);
        Assert.assertEquals(start, scale.toScaleUnits(0d));
        Assert.assertEquals(end, scale.toScaleUnits(168d));

        // Over-boundaries
        Assert.assertEquals(0d, scale.toGridPixels(start.minus(1, YEARS)), ACCEPTED_ERROR);
        Assert.assertEquals(84d, scale.toGridPixels(end.plus(1, YEARS)), ACCEPTED_ERROR);
        Assert.assertEquals(start, scale.toScaleUnits(-1000d));
        Assert.assertEquals(end, scale.toScaleUnits(10000000d));

        // 3 Days and 4 Hours after
        final LocalDateTime d = start.plus(3, DAYS).plus(4, HOURS);
        Assert.assertEquals(38d, scale.toGridPixels(d), ACCEPTED_ERROR);
        Assert.assertEquals(d, scale.toScaleUnits(38d));
    }
}
