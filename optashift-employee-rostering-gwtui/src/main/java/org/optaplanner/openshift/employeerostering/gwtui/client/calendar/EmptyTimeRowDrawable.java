package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import elemental2.dom.CanvasRenderingContext2D;

public class EmptyTimeRowDrawable extends AbstractDrawable implements TimeRowDrawable {
    int index;
    String groupId;
    
    public EmptyTimeRowDrawable(String groupId, int index) {
        this.groupId = groupId;
        this.index = index;
    }
    
    @Override
    public double getLocalX() {
        return 0;
    }

    @Override
    public double getLocalY() {
        return 0;
    }

    @Override
    public void doDrawAt(CanvasRenderingContext2D g, double x, double y) {
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public LocalDateTime getStartTime() {
        return LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
    }

    @Override
    public LocalDateTime getEndTime() {
        return LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
    }

    @Override
    void doDraw(CanvasRenderingContext2D g) {
    }

}
