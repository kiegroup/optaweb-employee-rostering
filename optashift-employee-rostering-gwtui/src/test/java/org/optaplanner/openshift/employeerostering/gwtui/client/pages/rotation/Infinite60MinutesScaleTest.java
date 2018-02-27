package org.optaplanner.openshift.employeerostering.gwtui.client.pages.rotation;

import org.junit.Assert;
import org.junit.Test;

public class Infinite60MinutesScaleTest {

    @Test
    public void test() {

        final Long start = 0L;
        final Long end = 10080L;

        final Infinite60MinutesScale scale = new Infinite60MinutesScale(end);

        // Boundaries
        Assert.assertEquals((Long) 0L, scale.toGridPixels(start));
        Assert.assertEquals((Long) 168L, scale.toGridPixels(end));
        Assert.assertEquals(start, scale.toScaleUnits(0L));
        Assert.assertEquals(end, scale.toScaleUnits(168L));

        // Over-boundaries
        Assert.assertEquals((Long) (-1L), scale.toGridPixels(start - 60L));
        Assert.assertEquals((Long) 192L, scale.toGridPixels(end + 1440L));
        Assert.assertEquals((Long) (-60L), scale.toScaleUnits(-1L));
        Assert.assertEquals((Long) 11520L, scale.toScaleUnits(192L));

        // 3 Days and 4 Hours after
        final Long d = start + 4560L;
        Assert.assertEquals((Long) 76L, scale.toGridPixels(d));
        Assert.assertEquals(d, scale.toScaleUnits(76L));
    }
}