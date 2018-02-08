package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.demo.Positive2HoursScale;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.YEARS;

public class Positive2HoursScaleTest {

    @Test
    public void test() {

        final LocalDateTime start = LocalDateTime.of(2018, 2, 7, 0, 0);
        final LocalDateTime end = start.plusDays(7);

        final Positive2HoursScale scale = new Positive2HoursScale(start, s -> end);

        // Boundaries
        Assert.assertEquals((Long) 0L, scale.toGridPixels(start));
        Assert.assertEquals((Long) 84L, scale.toGridPixels(end));
        Assert.assertEquals(start, scale.toScaleUnits(0L));
        Assert.assertEquals(end, scale.toScaleUnits(168L));

        // Over-boundaries
        Assert.assertEquals((Long) 0L, scale.toGridPixels(start.minus(1, YEARS)));
        Assert.assertEquals((Long) 84L, scale.toGridPixels(end.plus(1, YEARS)));
        Assert.assertEquals(start, scale.toScaleUnits(-1000L));
        Assert.assertEquals(end, scale.toScaleUnits(10000000L));

        // 3 Days and 4 Hours after
        final LocalDateTime d = start.plus(3, DAYS).plus(4, HOURS);
        Assert.assertEquals((Long) 38L, scale.toGridPixels(d));
        Assert.assertEquals(d, scale.toScaleUnits(38L));
    }
}