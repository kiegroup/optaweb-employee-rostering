/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers;

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;

// Used for collision detection only
public class Outline implements Blob {

    private Integer size;
    private Integer position;

    public static Blob of(final Integer position, final Integer size) {
        return new Outline(position, size);
    }

    private Outline(final Integer position, final Integer size) {
        this.position = position;
        this.size = size;
    }

    @Override
    public String getLabel() {
        return "";
    }

    @Override
    public void setLabel(String label) {
    }

    @Override
    public Integer getSize() {
        return size;
    }

    @Override
    public Integer getPosition() {
        return position;
    }

    @Override
    public void setPosition(final Integer position) {
        this.position = position;
    }

    @Override
    public void setSize(final Integer size) {
        this.size = size;
    }
}
