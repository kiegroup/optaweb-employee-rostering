package org.optaplanner.openshift.employeerostering.gwtui.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.jboss.errai.common.client.api.elemental2.IsElement;

@Singleton
public class PageUtils {

    private Collection<IsElement> heightEaterElements;
    private Collection<IsElement> widthEaterElements;

    private double verticalPadding;
    private double horizontalPadding;

    @PostConstruct
    public void init() {
        widthEaterElements = new ArrayList<>();
        heightEaterElements = new ArrayList<>();
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

    public PageUtils addWidthEaterElements(IsElement... elements) {
        widthEaterElements.addAll(Arrays.asList(elements));
        return this;
    }

    public PageUtils addHeightEaterElements(IsElement... elements) {
        heightEaterElements.addAll(Arrays.asList(elements));
        return this;
    }

    public PageUtils removeWidthEaterElements(IsElement... elements) {
        widthEaterElements.removeAll(Arrays.asList(elements));
        return this;
    }

    public PageUtils removeHeightEaterElements(IsElement... elements) {
        heightEaterElements.removeAll(Arrays.asList(elements));
        return this;
    }

    public void expandElementToRemainingHeight(IsElement element) {
        double totalHeightEaten = heightEaterElements.stream().map(e -> e.getElement().scrollHeight).reduce((a, b) -> a + b).orElseGet(() -> Double.valueOf(0));
        totalHeightEaten += verticalPadding;
        element.getElement().style.set("height", "calc(100vh - " +
                totalHeightEaten + "px)");
    }

    public void expandElementToRemainingWidth(IsElement element) {
        double totalWidthEaten = widthEaterElements.stream().map(e -> e.getElement().scrollWidth).reduce((a, b) -> a + b).orElseGet(() -> Double.valueOf(0));
        totalWidthEaten += horizontalPadding;
        element.getElement().style.set("width", "calc(100vw - " +
                totalWidthEaten + "px)");
    }
}
