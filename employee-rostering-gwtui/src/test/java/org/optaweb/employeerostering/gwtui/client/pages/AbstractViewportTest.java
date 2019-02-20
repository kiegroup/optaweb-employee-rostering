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

package org.optaweb.employeerostering.gwtui.client.pages;

import elemental2.dom.CSSStyleDeclaration;
import elemental2.dom.DOMTokenList;
import elemental2.dom.HTMLElement;
import elemental2.promise.IThenable;
import elemental2.promise.IThenable.ThenOnFulfilledCallbackFn;
import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn.ResolveUnionType;
import jsinterop.annotations.JsOverlay;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.LinearScale;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.SingleGridObject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractViewportTest {

    PromiseExecutorCallbackFn rootPromise;

    protected SingleGridObject<Double, Object> getSingleGridObject(Double start, Double end) {
        return new MockGridObject(start, end);
    }

    protected DoubleScale getScale(Double end) {
        return new DoubleScale(end);
    }

    @SuppressWarnings("unchecked")
    public <T> Promise<T> promise(PromiseExecutorCallbackFn callback) {
        Promise<T> promise = mock(Promise.class);
        if (rootPromise == null) {
            rootPromise = callback;
        }
        when(promise.then(any())).thenAnswer(invocation -> {
            ThenOnFulfilledCallbackFn onFulfilled = invocation.getArgument(0);
            Object[] out = new Object[1];
            rootPromise.onInvoke((res) -> {
                out[0] = onFulfilled.onInvoke(res.asT());
            }, (rej) -> {
            });
            rootPromise = getCallbackFn(out[0]);
            return promise(rootPromise);
        });
        return promise;
    }

    private PromiseExecutorCallbackFn getCallbackFn(Object val) {
        return (resolve, reject) -> {
            resolve.onInvoke(resolveValue(val));
            rootPromise = null;
        };
    }

    protected <T> ResolveUnionType<T> resolveValue(T value) {
        return new MockResolveUnionType<>(value);
    }

    private static class MockResolveUnionType<T> implements ResolveUnionType<T> {

        private T value;

        public MockResolveUnionType(T value) {
            this.value = value;
        }

        @JsOverlay
        public IThenable<T> asIThenable() {
            return null;
        }

        @JsOverlay
        public T asT() {
            return value;
        }
    }

    private static class DoubleScale implements LinearScale<Double> {

        private Double end;

        public DoubleScale(Double end) {
            this.end = end;
        }

        @Override
        public double toGridUnits(Double valueInScaleUnits) {
            return valueInScaleUnits;
        }

        @Override
        public Double toScaleUnits(double valueInGridPixels) {
            return valueInGridPixels;
        }

        @Override
        public Double getEndInScaleUnits() {
            return end;
        }
    }

    private static class MockGridObject implements SingleGridObject<Double, Object> {

        private Double start;
        private Double end;
        private HTMLElement element;
        private Lane<Double, Object> lane;

        public MockGridObject(Double start, Double end) {
            this.start = start;
            this.end = end;
            element = mock(HTMLElement.class);
            element.style = mock(CSSStyleDeclaration.class);
            element.classList = mock(DOMTokenList.class);
        }

        @Override
        public Double getStartPositionInScaleUnits() {
            return start;
        }

        @Override
        public void setStartPositionInScaleUnits(Double newStartPosition) {
            this.start = newStartPosition;
        }

        @Override
        public Double getEndPositionInScaleUnits() {
            return end;
        }

        @Override
        public void setEndPositionInScaleUnits(Double newEndPosition) {
            this.end = newEndPosition;
        }

        @Override
        public void withLane(Lane<Double, Object> lane) {
            this.lane = lane;
        }

        @Override
        public Long getId() {
            return null;
        }

        @Override
        public Lane<Double, Object> getLane() {
            return lane;
        }

        @Override
        public void save() {
        }

        @Override
        public HTMLElement getElement() {
            return element;
        }
    }
}
