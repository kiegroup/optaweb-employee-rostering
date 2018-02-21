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

import java.util.function.BiConsumer;

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.CollisionDetector;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;

import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.CollisionState.COLLIDING;
import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.CollisionState.NOT_COLLIDING;

public class CircularBlobChangeHandler<T, Y extends BlobWithTwin<T, Y>> {

    private final Y blob;
    private final ListView<Y> blobViews;
    private final CollisionDetector<Blob<T>> collisionDetector;
    private final Viewport<T> viewport;

    private BiConsumer<Long, CollisionState> onChange;

    CircularBlobChangeHandler(final Y blob,
                              final ListView<Y> blobViews,
                              final CollisionDetector<Blob<T>> collisionDetector,
                              final Viewport<T> viewport) {

        this.blob = blob;
        this.viewport = viewport;
        this.collisionDetector = collisionDetector;
        this.blobViews = blobViews;
    }

    public void handle(final Long newPositionInGridPixels, final Long newSizeInGridPixels) {

        blob.setSizeInGridPixels(newSizeInGridPixels);
        blob.setPositionInScaleUnits(viewport.getScale().toScaleUnits(newPositionInGridPixels));

        blob.getTwin().ifPresent(blobViews::remove);
        blob.setTwin(blob.getUpdatedTwin());
        blob.getTwin().ifPresent(blobViews::add);

        final boolean anyCollisionDetected =
                collisionDetector.collides(blob) ||
                        blob.getTwin().map(collisionDetector::collides).orElse(false);

        if (anyCollisionDetected) {
            paintBlobsBackground("red");
            onChange.accept(newPositionInGridPixels, COLLIDING);
        } else {
            paintBlobsBackground("");
            onChange.accept(newPositionInGridPixels, NOT_COLLIDING);
        }
    }

    //FIXME: This is a side-effect used in development only
    private void paintBlobsBackground(final String backgroundColor) {
        blobViews.getView(blob).getElement().style.backgroundColor = backgroundColor;
        blob.getTwin().map(blobViews::getView).ifPresent(view -> view.getElement().style.backgroundColor = backgroundColor);
    }

    public void onChange(final BiConsumer<Long, CollisionState> onChange) {
        this.onChange = onChange;
    }
}
