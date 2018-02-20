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

public class BlobChangeHandler<T, Y extends BlobWithTwin<T, Y>> {

    private final Y blob;
    private final Viewport<T> viewport;
    private final CollisionDetector<Blob<T>> collisionDetector;
    private final ListView<Y> list;

    private BiConsumer<Long, CollisionState> onChange;

    BlobChangeHandler(final Y blob,
                      final ListView<Y> list,
                      final CollisionDetector<Blob<T>> collisionDetector,
                      final Viewport<T> viewport) {

        this.blob = blob;
        this.viewport = viewport;
        this.collisionDetector = collisionDetector;
        this.list = list;
    }

    public void handle(final Long newPositionInGridPixels, final Long newSizeInGridPixels) {

        blob.setSizeInGridPixels(newSizeInGridPixels);
        blob.setPositionInScaleUnits(viewport.getScale().toScaleUnits(newPositionInGridPixels));

        createOrRemoveTwin(newPositionInGridPixels, newSizeInGridPixels);

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

    private void createOrRemoveTwin(final Long positionInGridPixels,
                                    final Long sizeInGridPixels) {

        final boolean hasAnyPartOffTheGrid =
                blob.getEndPositionInGridPixels() > viewport.getSizeInGridPixels() ||
                        blob.getPositionInGridPixels() < 0;

        if (hasAnyPartOffTheGrid) {
            final Y twin = blob.getTwin().orElseGet(blob::makeTwin);
            final Long offset = (positionInGridPixels < 0 ? 1 : -1) * viewport.getSizeInGridPixels();
            twin.setPositionInScaleUnits(viewport.getScale().toScaleUnits(positionInGridPixels + offset));
            twin.setSizeInGridPixels(sizeInGridPixels);
            list.addIfNotPresent(twin);
        } else {
            blob.getTwin().ifPresent(twin -> {
                list.remove(twin);
                blob.setTwin(null);
            });
        }
    }

    //FIXME: This is a side-effect used in development only
    private void paintBlobsBackground(final String backgroundColor) {
        list.getView(blob).getElement().style.backgroundColor = backgroundColor;
        blob.getTwin().map(list::getView).ifPresent(v -> v.getElement().style.backgroundColor = backgroundColor);
    }

    public void onChange(final BiConsumer<Long, CollisionState> onChange) {
        this.onChange = onChange;
    }
}
