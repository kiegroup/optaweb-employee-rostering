package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.AbstractViewportTest;

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
