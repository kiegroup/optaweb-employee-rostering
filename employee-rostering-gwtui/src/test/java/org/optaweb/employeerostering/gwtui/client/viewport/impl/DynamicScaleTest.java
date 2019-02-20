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

package org.optaweb.employeerostering.gwtui.client.viewport.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.Assert;
import org.junit.Test;

public class DynamicScaleTest {

    private static final Double ACCEPTED_ERROR = Double.MIN_VALUE;

    @Test
    public void testValidScale() {
        testScale(LocalDateTime.of(LocalDate.of(2005, 4, 4), LocalTime.MIDNIGHT), Duration.ofDays(14), Duration.ofHours(4));
        testScale(LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.MIDNIGHT), Duration.ofDays(7), Duration.ofHours(6));
    }

    @Test
    public void testInvalidScale() {
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.of(2005, 4, 4), LocalTime.MIDNIGHT);
        LocalDateTime endDateTime = startDateTime.plusDays(21);
        Duration gridPixelLength = Duration.ofMinutes(30);

        try {
            // 48 grid units per day, 48 * 21 = 1008, which is greater than 1000 (CSS grid limit)
            final DynamicScale scale = new DynamicScale(startDateTime, endDateTime, gridPixelLength);
            Assert.fail("scale is invalid; an IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains(startDateTime.toString()));
            Assert.assertTrue(e.getMessage().contains(endDateTime.toString()));
            Assert.assertTrue(e.getMessage().contains(gridPixelLength.toString()));
        } catch (Throwable e) {
            Assert.fail("Expected an IllegalArgumentException, but got (" + e + ").");
        }
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
