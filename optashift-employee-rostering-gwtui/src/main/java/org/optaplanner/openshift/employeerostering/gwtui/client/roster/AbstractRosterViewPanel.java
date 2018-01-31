package org.optaplanner.openshift.employeerostering.gwtui.client.roster;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Span;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;

import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.*;

public abstract class AbstractRosterViewPanel implements Observer, IsElement {

    protected static final int REFRESH_RATE = 2000;

    @Inject
    @DataField
    protected Button solveButton;
    @Inject
    @DataField
    protected Button refreshButton;
    @Inject
    @DataField
    protected Span solveStatus;
    @Inject
    @DataField
    protected Span rosterScore;

    protected static Observable solverObservable = new Observable();

    protected Span buttonContent;
    protected Span buttonImage;
    protected Span buttonText;
    private boolean isSolving = false;
    @Inject
    private TranslationService CONSTANTS;

    @Inject
    private TenantStore tenantStore;

    protected void init() {
        buttonContent = new Span();
        buttonImage = new Span();
        buttonText = new Span();
        solveButton.getElement().setAttribute("class", "btn btn-success");
        buttonImage.getElement().setAttribute("class", "glyphicon glyphicon-play");
        buttonImage.getElement().setAttribute("aria-hidden", "true");
        buttonText.setHTML(new SafeHtmlBuilder()
                .appendEscaped(CONSTANTS.format("AbstractRosterViewPanel.solve"))
                .toSafeHtml().asString());
        buttonContent.add(buttonImage);
        buttonContent.add(buttonText);
        solveButton.add(buttonContent);
        solverObservable.addObserver(this);
    }

    public void onAnyTenantEvent(@Observes TenantStore.TenantChange tenantChange) {
        refresh();
    }

    @EventHandler("refreshButton")
    public void refresh(ClickEvent e) {
        refresh();
    }

    public void refresh() {
        refreshTable();
    }

    public void update(Observable observable, Object arg) {
        if (solverObservable == observable) {
            if (arg instanceof StartSolvingEvent) {
                solveButton.getElement().setAttribute("class", "btn btn-danger");
                buttonImage.getElement().setAttribute("class", "glyphicon glyphicon-stop");
                buttonImage.getElement().setAttribute("aria-hidden", "true");
                buttonText.setHTML(new SafeHtmlBuilder()
                        .appendEscaped(CONSTANTS.format("AbstractRosterViewPanel.terminateEarly"))
                        .toSafeHtml().asString());
                isSolving = true;
            } else if (arg instanceof TerminateSolvingEvent) {
                solveButton.getElement().setAttribute("class", "btn btn-success");
                buttonImage.getElement().setAttribute("class", "glyphicon glyphicon-play");
                buttonImage.getElement().setAttribute("aria-hidden", "true");
                buttonText.setHTML(new SafeHtmlBuilder()
                        .appendEscaped(CONSTANTS.format("AbstractRosterViewPanel.solve"))
                        .toSafeHtml().asString());
                isSolving = false;
                solveStatus.setHTML(new SafeHtmlBuilder()
                        .appendHtmlConstant(CONSTANTS.format(AbstractRosterViewPanel_finishedSolving) + ".")
                        .toSafeHtml().asString());
            } else if (arg instanceof SetSolvingTimeEvent) {
                SetSolvingTimeEvent event = (SetSolvingTimeEvent) arg;
                solveStatus.setHTML(new SafeHtmlBuilder()
                        .appendHtmlConstant(CONSTANTS.format(AbstractRosterViewPanel_solvingFor, event
                                .getSecondsRemaining()))
                        .toSafeHtml().asString());
            } else if (arg instanceof RosterUpdateEvent) {
                RosterUpdateEvent event = (RosterUpdateEvent) arg;
                HardSoftScore score = event.getRoster().getScore();
                if (null != score) {

                    rosterScore.setHTML(new SafeHtmlBuilder()
                            .appendEscaped("Hard Score: ").append(score.getHardScore())
                            .appendEscaped(", Soft Score: ").append(score.getSoftScore())
                            .toSafeHtml().asString());
                }
            }
        } else {
            solverObservable.notifyObservers(arg);
        }
    }

    protected abstract void refreshTable();

    @EventHandler("solveButton")
    public void solve(ClickEvent e) {
        if (getTenantId() == null) {
            throw new IllegalStateException("The tenantId (" + getTenantId() + ") cannot be null at this time.");
        }
        if (isSolving) {
            RosterRestServiceBuilder.terminateRosterEarly(getTenantId(), new FailureShownRestCallback<Void>() {

                public void onSuccess(Void t) {
                    solverObservable.notifyObservers(new TerminateSolvingEvent());
                }
            });
        } else {
            RosterRestServiceBuilder.solveRoster(getTenantId(), new FailureShownRestCallback<Void>() {

                public void onSuccess(Void t) {
                    solverObservable.notifyObservers(new StartSolvingEvent());
                    // TODO 15 * 2000ms = 30 seconds - Keep in sync with solver config
                    Scheduler.get().scheduleFixedDelay(new RefreshTableRepeatingCommand(15), REFRESH_RATE);
                }
            });
        }
    }

    protected class StartSolvingEvent {
    }

    protected class TerminateSolvingEvent {
    }

    protected class SetSolvingTimeEvent {

        final int secondsRemaining;

        public SetSolvingTimeEvent(int secondRemaining) {
            this.secondsRemaining = secondRemaining;
        }

        public int getSecondsRemaining() {
            return secondsRemaining;
        }
    }

    protected class RefreshTableRepeatingCommand implements Scheduler.RepeatingCommand, Observer {

        private int repeatCount;
        boolean terminateEarly = false;

        public RefreshTableRepeatingCommand(int repeatCount) {
            this.repeatCount = repeatCount;
            solverObservable.addObserver(this);
            updateSolverStatus();
        }

        @Override
        public boolean execute() {
            if (terminateEarly) {
                solverObservable.removeObserver(this);
                return false;
            }
            refreshTable();
            // To repeat n times, return true only n-1 times.
            repeatCount--;
            if (repeatCount > 0) {
                updateSolverStatus();
                return true;
            } else {
                solverObservable.removeObserver(this);
                solverObservable.notifyObservers(new TerminateSolvingEvent());
                return false;
            }
        }

        private void updateSolverStatus() {
            int remainingSeconds = REFRESH_RATE * repeatCount / 1000;
            solverObservable.notifyObservers(new SetSolvingTimeEvent(remainingSeconds));

        }

        @Override
        public void update(Observable observable, Object argument) {
            if (observable == solverObservable && argument instanceof TerminateSolvingEvent) {
                terminateEarly = true;
            }
        }

    }

    public Integer getTenantId() {
        return tenantStore.getCurrentTenantId();
    }
}
