package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.YEARS;

public class PositiveMinutesScaleTest {

    @Test
    public void test() {

        final LocalDateTime start = LocalDateTime.of(2018, 2, 7, 0, 0);
        final LocalDateTime end = start.plusDays(7);

        final PositiveMinutesScale scale = new PositiveMinutesScale(start, end);

        // Boundaries
        Assert.assertEquals((Long) 0L, scale.to(start));
        Assert.assertEquals((Long) 10080L, scale.to(end));
        Assert.assertEquals(start, scale.from(0L));
        Assert.assertEquals(end, scale.from(10080L));

        // Over-boundaries
        Assert.assertEquals((Long) 0L, scale.to(start.minus(1, YEARS)));
        Assert.assertEquals((Long) 10080L, scale.to(end.plus(1, YEARS)));
        Assert.assertEquals(start, scale.from(-1000L));
        Assert.assertEquals(end, scale.from(10000000L));

        // 3 Days and 3 Hours after
        final LocalDateTime d = start.plus(3, DAYS).plus(3, HOURS);
        Assert.assertEquals((Long) 4500L, scale.to(d));
        Assert.assertEquals(d, scale.from(4500L));
    }
}