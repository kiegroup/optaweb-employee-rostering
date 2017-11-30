package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.HTMLCanvasElement;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;

@Templated
public class MockScrollBar implements IsElement {

    @Inject
    @DataField
    private Button nextButton;

    @Inject
    @DataField
    private Button previousButton;

    @Inject
    @DataField
    private HTMLCanvasElement scrollbar;

    private Action nextButtonAction = () -> {
    };

    private Action previousButtonAction = () -> {
    };

    private int contentShown = 1;

    private int contentSize = 1;

    private int contentIndex = 0;

    public void draw() {
        CanvasRenderingContext2D g = (CanvasRenderingContext2D) (Object) scrollbar.getContext("2d");
        double width = scrollbar.width;
        double height = scrollbar.height;

        double widthOfContent = width / (contentSize + 0.0);

        CanvasUtils.setFillColor(g, "#FFFF00");
        g.fillRect(0, 0, widthOfContent * contentIndex, height);

        CanvasUtils.setFillColor(g, "#000000");
        g.fillRect(widthOfContent * contentIndex, 0, widthOfContent * contentShown, height);

        CanvasUtils.setFillColor(g, "#FFFF00");
        g.fillRect(widthOfContent * (contentIndex + contentShown), 0, widthOfContent * (contentSize - contentIndex
                - contentShown), height);
    }

    public void setContentSize(int contentSize) {
        this.contentSize = contentSize;
        draw();
    }

    public void setContentShown(int contentShown) {
        this.contentShown = contentShown;
        draw();
    }

    public void setContentIndex(int contentIndex) {
        this.contentIndex = contentIndex;
        draw();
    }

    public void setNextButtonAction(Action action) {
        nextButtonAction = action;
    }

    public void setPreviousButtonAction(Action action) {
        previousButtonAction = action;
    }

    @EventHandler("nextButton")
    public void onNextButtonClick(ClickEvent e) {
        nextButtonAction.performAction();
    }

    @EventHandler("previousButton")
    public void onPreviousButtonClick(ClickEvent e) {
        previousButtonAction.performAction();
    }

    public interface Action {

        void performAction();
    }
}
