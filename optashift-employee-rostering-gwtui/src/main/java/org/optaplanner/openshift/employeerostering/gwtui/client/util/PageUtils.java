package org.optaplanner.openshift.employeerostering.gwtui.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.jboss.errai.common.client.api.elemental2.IsElement;

@Singleton
public class PageUtils {

    private Collection<IsElement> heightConsumingElements = new ArrayList<>();;
    private Collection<IsElement> widthConsumingElements = new ArrayList<>();

    private double verticalPadding;
    private double horizontalPadding;

    @PostConstruct
    public void init() {
        verticalPadding = 0;
        horizontalPadding = 0;
    }

    public PageUtils withVerticalPadding(double padding) {
        verticalPadding = padding;
        return this;
    }

    public PageUtils withHorizontalPadding(double padding) {
        horizontalPadding = padding;
        return this;
    }

    public PageUtils addWidthConsumingElements(IsElement... elements) {
        widthConsumingElements.addAll(Arrays.asList(elements));
        return this;
    }

    public PageUtils addHeightConsumingElements(IsElement... elements) {
        heightConsumingElements.addAll(Arrays.asList(elements));
        return this;
    }

    public PageUtils removeWidthConsumingElements(IsElement... elements) {
        widthConsumingElements.removeAll(Arrays.asList(elements));
        return this;
    }

    public PageUtils removeHeightConsumingElements(IsElement... elements) {
        heightConsumingElements.removeAll(Arrays.asList(elements));
        return this;
    }

    public void expandElementToRemainingHeight(IsElement element) {
        double totalHeightConsumed = heightConsumingElements.stream().map(e -> e.getElement().scrollHeight).reduce((a, b) -> a + b).orElseGet(() -> Double.valueOf(0));
        totalHeightConsumed += verticalPadding;
        element.getElement().style.set("height", "calc(100vh - " +
                totalHeightConsumed + "px)");
    }

    public void expandElementToRemainingWidth(IsElement element) {
        double totalWidthConsumed = widthConsumingElements.stream().map(e -> e.getElement().scrollWidth).reduce((a, b) -> a + b).orElseGet(() -> Double.valueOf(0));
        totalWidthConsumed += horizontalPadding;
        element.getElement().style.set("width", "calc(100vw - " +
                totalWidthConsumed + "px)");
    }
}
