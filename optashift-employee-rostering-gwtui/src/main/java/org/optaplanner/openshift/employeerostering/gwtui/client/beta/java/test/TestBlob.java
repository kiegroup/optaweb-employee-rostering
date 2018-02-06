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

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.test;

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;

public class TestBlob implements Blob {

    private Integer size;
    private Integer position;
    private String label;

    public TestBlob(final String label,
                    final Integer size,
                    final Integer position) {

        this.size = size;
        this.position = position;
        this.label = label;
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
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(final String label) {
        this.label = label;
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
