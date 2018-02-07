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

public class TestBlob implements Blob<Long> {

    private Long size;
    private Long position;
    private String label;

    public TestBlob(final String label,
                    final Long size,
                    final Long position) {

        this.size = size;
        this.position = position;
        this.label = label;
    }

    @Override
    public Long getSizeInGridPixels() {
        return size;
    }

    @Override
    public Long getPosition() {
        return position;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    @Override
    public void setPosition(final Long position) {
        this.position = position;
    }

    @Override
    public void setSizeInGridPixels(final Long sizeInGridPixels) {
        this.size = sizeInGridPixels;
    }
}
