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

package org.optaweb.employeerostering.gwtui.client.viewport.grid;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.optaweb.employeerostering.gwtui.client.pages.AbstractViewportTest;

@RunWith(GwtMockitoTestRunner.class)
public class GridObjectPlacerTest extends AbstractViewportTest {

    GridObjectPlacer placer;
    LinearScale<Double> scale;
    SingleGridObject<Double, ?> gridObject;

    @Before
    public void setup() {
        placer = GridObjectPlacer.HORIZONTAL;
        scale = super.getScale(100d);
        gridObject = super.getSingleGridObject(0d, 1d);
    }

    @Test
    public void testSetStartPositionWithSnap() {
        placer.setStartPositionInScaleUnits(gridObject, scale, 2.25d, true);
        Assert.assertEquals(2d, gridObject.getStartPositionInScaleUnits(), Double.MIN_VALUE);
        Assert.assertEquals(1d, gridObject.getEndPositionInScaleUnits(), Double.MIN_VALUE);
        Mockito.verify(gridObject.getElement().style).set("grid-column-start", "3");
        Mockito.verify(gridObject.getElement().classList).remove("hidden");
    }

    @Test
    public void testSetStartPositionWithNoSnap() {
        placer.setStartPositionInScaleUnits(gridObject, scale, 2.25d, false);
        Assert.assertEquals(2.25d, gridObject.getStartPositionInScaleUnits(), Double.MIN_VALUE);
        Assert.assertEquals(1d, gridObject.getEndPositionInScaleUnits(), Double.MIN_VALUE);
        Mockito.verify(gridObject.getElement().style).set("grid-column-start", "3");
        Mockito.verify(gridObject.getElement().style).set("margin-left", "calc(0.25*" + GridObjectPlacer.GRID_UNIT_SIZE + ")");
        Mockito.verify(gridObject.getElement().classList).remove("hidden");
    }

    @Test
    public void testSetEndPositionWithSnap() {
        placer.setEndPositionInScaleUnits(gridObject, scale, 2.25d, true);
        Assert.assertEquals(0d, gridObject.getStartPositionInScaleUnits(), Double.MIN_VALUE);
        Assert.assertEquals(2d, gridObject.getEndPositionInScaleUnits(), Double.MIN_VALUE);
        Mockito.verify(gridObject.getElement().style).set("grid-column-end", "3");
        Mockito.verify(gridObject.getElement().classList).remove("hidden");
    }

    @Test
    public void testSetEndPositionWithNoSnap() {
        placer.setEndPositionInScaleUnits(gridObject, scale, 2.25d, false);
        Assert.assertEquals(0d, gridObject.getStartPositionInScaleUnits(), Double.MIN_VALUE);
        Assert.assertEquals(2.25d, gridObject.getEndPositionInScaleUnits(), Double.MIN_VALUE);
        Mockito.verify(gridObject.getElement().style).set("grid-column-end", "3");
        Mockito.verify(gridObject.getElement().style).set("margin-right", "calc(0.25*" + GridObjectPlacer.GRID_UNIT_SIZE + ")");
        Mockito.verify(gridObject.getElement().classList).remove("hidden");
    }

    // TODO: More tests
}
