package org.optaplanner.openshift.employeerostering.gwtui.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Window;
import org.jboss.errai.common.client.api.elemental2.IsElement;

@ApplicationScoped
public class PageUtils {

    private List<IsElementCollection> heightConsumingElements = new ArrayList<>();
    private List<IsElementCollection> widthFillingElements = new ArrayList<>();
    private IsElement page;

    public PageUtils appendWidthFillingElements(IsElement... elements) {
        for (IsElement element : elements) {
            widthFillingElements.add(() -> Collections.singletonList(element));
        }
        refresh();
        return this;
    }

    public PageUtils appendWidthFillingElementsAsRow(List<IsElement> elements) {
        widthFillingElements.add(() -> elements);
        refresh();
        return this;
    }

    public PageUtils removeWidthFillingElementsAfterAndIncluding(int index) {
        List<IsElementCollection> elements = widthFillingElements.subList(index, heightConsumingElements.size());
        elements.forEach((c) -> c.getElements().forEach((e) -> e.getElement().remove()));
        elements.clear();
        refresh();
        return this;
    }

    public PageUtils removeWidthFillingElementssAsRow(List<IsElement> elements) {
        widthFillingElements.removeIf((c) -> c.getElements().stream().anyMatch((e) -> elements.contains(e)));
        refresh();
        return this;
    }

    public PageUtils removeWidthFillingElements(IsElement... elements) {
        List<IsElement> elementList = Arrays.asList(elements);
        widthFillingElements.removeIf((c) -> c.getElements().stream().anyMatch((e) -> elementList.contains(e)));
        refresh();
        return this;
    }

    public PageUtils appendHeightConsumingElements(IsElement... elements) {
        for (IsElement element : elements) {
            heightConsumingElements.add(() -> Collections.singletonList(element));
        }
        refresh();
        return this;
    }

    public PageUtils appendHeightConsumingElementsAsRow(List<IsElement> elements) {
        heightConsumingElements.add(() -> elements);
        refresh();
        return this;
    }

    public PageUtils removeHeightConsumingElementsAfterAndIncluding(int index) {
        List<IsElementCollection> elements = heightConsumingElements.subList(index, heightConsumingElements.size());
        elements.forEach((c) -> c.getElements().forEach((e) -> e.getElement().remove()));
        elements.clear();
        refresh();
        return this;
    }

    public PageUtils removeHeightConsumingElementsAsRow(List<IsElement> elements) {
        heightConsumingElements.removeIf((c) -> c.getElements().stream().anyMatch((e) -> elements.contains(e)));
        refresh();
        return this;
    }

    public PageUtils removeHeightConsumingElements(IsElement... elements) {
        List<IsElement> elementList = Arrays.asList(elements);
        heightConsumingElements.removeIf((c) -> c.getElements().stream().anyMatch((e) -> elementList.contains(e)));
        refresh();
        return this;
    }

    public PageUtils setPage(IsElement page) {
        this.page = page;
        return this;
    }

    public PageUtils makePageScrollable() {
        page.getElement().style.set("width", Document.get().getScrollWidth() + "px");
        page.getElement().style.set("height", Document.get().getScrollHeight() + "px");
        return this;
    }

    public native int getVerticalScrollbarWidth() /*-{ 
        var outer = document.createElement("div"); 
        outer.style.visibility = "hidden"; 
        outer.style.width = "100px"; 
        outer.style.msOverflowStyle = "scrollbar"; // needed for WinJS apps
        document.body.appendChild(outer);
        var widthNoScroll = outer.offsetWidth;
        // force scrollbars
        outer.style.overflow = "scroll";
        // add innerdiv
        var inner = document.createElement("div");
        inner.style.width = "100%";
        outer.appendChild(inner);                 
        var widthWithScroll = inner.offsetWidth;                                          
        // remove divs
        outer.parentNode.removeChild(outer);
        return widthNoScroll - widthWithScroll;
    }-*/;

    public native int getHorizontalScrollbarHeight() /*-{
        var outer = document.createElement("div");
        outer.style.visibility = "hidden";
        outer.style.height = "100px";
        outer.style.msOverflowStyle = "scrollbar"; // needed for WinJS apps               
        document.body.appendChild(outer);
        heightNoScroll = outer.offsetHeight;
        // force scrollbars
        outer.style.overflow = "scroll";
        // add innerdiv
        var inner = document.createElement("div");
        inner.style.height = "100%";
        outer.appendChild(inner);        
        var heightWithScroll = inner.offsetHeight;
        // remove divs
        outer.parentNode.removeChild(outer);                                             
        return heightNoScroll - heightWithScroll;
    }-*/;

    public int getWindowWidth() {
        return Window.getClientWidth();
    }

    public int getWindowHeight() {
        return Window.getClientHeight();
    }

    public PageUtils resetPageToDefault() {
        page.getElement().style.set("width", "100%");
        page.getElement().style.set("height", "100%");
        return this;
    }

    public void refresh() {
        for (IsElementCollection element : widthFillingElements) {
            if (page != null && page.getElement().scrollWidth != page.getElement().offsetWidth) {
                element.getElements().forEach((e) -> e.getElement().style.set("width", "calc(100vw - " + getVerticalScrollbarWidth() + "px)"));
            } else {
                element.getElements().forEach((e) -> e.getElement().style.set("width", "100vw"));
            }
        }

        for (int i = 0; i < heightConsumingElements.size(); i++) {
            for (IsElement element : heightConsumingElements.get(i).getElements()) {
                if (i == 0) {
                    element.getElement().style.top = "calc(0px)";
                } else {
                    IsElement last = heightConsumingElements.get(i - 1).getElements().get(0);
                    String height = (last.getElement().style.height.asString().isEmpty()) ? last.getElement().getBoundingClientRect().height + "px" : last.getElement().style.height.asString();
                    element.getElement().style.top = "calc(" +
                                                     last.getElement().style.top.substring(5, last.getElement().style.top.length() - 1) + " + " +
                                                     height + ")";
                }
            }
        }
    }

    public int getHeightConsumed() {
        int out = 0;
        for (int i = 1; i < heightConsumingElements.size(); i++) {
            IsElement last = heightConsumingElements.get(i - 1).getElements().get(0);
            out += last.getElement().scrollHeight;
        }
        return out;
    }

    private static interface IsElementCollection {

        // List is not empty
        List<IsElement> getElements();
    }
}
