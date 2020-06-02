/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.domain.violation;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.shift.Shift;


public class NoBreakViolation implements ConstraintMatchView {
    
    private Shift firstShift;
    private Shift secondShift;
    private Shift thirdShift;
    
    private HardMediumSoftLongScore score;

    public NoBreakViolation() {

    }

    public NoBreakViolation(Shift firstShift, Shift secondShift, Shift thirdShift, HardMediumSoftLongScore score) {
        this.firstShift = firstShift;
        this.secondShift = secondShift;
        this.thirdShift = thirdShift;
        this.score = score;
    }
    
    public Shift getFirstShift() {
        return firstShift;
    }

    
    public void setFirstShift(Shift firstShift) {
        this.firstShift = firstShift;
    }

    
    public Shift getSecondShift() {
        return secondShift;
    }

    
    public void setSecondShift(Shift secondShift) {
        this.secondShift = secondShift;
    }

    
    public Shift getThirdShift() {
        return thirdShift;
    }

    
    public void setThirdShift(Shift thirdShift) {
        this.thirdShift = thirdShift;
    }

    @Override
    public HardMediumSoftLongScore getScore() {
        return score;
    }
    
    public void setScore(HardMediumSoftLongScore score) {
        this.score = score;
    }

}
