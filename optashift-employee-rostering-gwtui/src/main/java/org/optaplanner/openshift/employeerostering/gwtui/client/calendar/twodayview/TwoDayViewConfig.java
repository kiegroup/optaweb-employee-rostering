package org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview;

import java.time.LocalDateTime;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.DateDisplay;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawableProvider;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

public class TwoDayViewConfig<G extends HasTitle, I extends HasTimeslot<G>, D extends TimeRowDrawable<G, I>> {

    private TwoDayViewPresenter<G, I, D> presenter;

    private LocalDateTime hardStartDateBound;
    private LocalDateTime hardEndDateBound;

    private TimeRowDrawableProvider<G, I, D> drawableProvider;
    private DateDisplay dateFormat;
    private TranslationService translator;

    private int daysShown;
    private int editMinuteGradality;
    private int displayMinuteGradality;

    public TwoDayViewConfig(TwoDayViewPresenter<G, I, D> presenter, TranslationService translator,
            DateDisplay dateFormat, TimeRowDrawableProvider<G, I, D> drawableProvider) {
        this.presenter = presenter;
        this.translator = translator;
        this.dateFormat = dateFormat;
        this.drawableProvider = drawableProvider;
        daysShown = 5;
        editMinuteGradality = 30;
        displayMinuteGradality = 60 * 3;
    }

    public LocalDateTime getHardStartDateBound() {
        return hardStartDateBound;
    }

    public void setHardStartDateBound(LocalDateTime hardStartDateBound) {
        this.hardStartDateBound = hardStartDateBound;
        if (presenter.getViewStartDate().isBefore(hardStartDateBound)) {
            presenter.setDate(hardStartDateBound);
        }
        presenter.draw();
    }

    public LocalDateTime getHardEndDateBound() {
        return hardEndDateBound;
    }

    public void setHardEndDateBound(LocalDateTime hardEndDateBound) {
        this.hardEndDateBound = hardEndDateBound;
        if (presenter.getViewEndDate().isAfter(hardEndDateBound)) {
            presenter.setDate(hardEndDateBound.minusDays(daysShown));
        }
        presenter.draw();
    }

    public TranslationService getTranslator() {
        return translator;
    }

    public DateDisplay getDateFormat() {
        return dateFormat;
    }

    public TimeRowDrawableProvider<G, I, D> getDrawableProvider() {
        return drawableProvider;
    }

    public int getDaysShown() {
        return daysShown;
    }

    public void setDaysShown(int daysShown) {
        if (this.daysShown == daysShown) {
            return;
        }
        this.daysShown = daysShown;
        presenter.setToolBox(null);
        presenter.getCalendar().setViewSize(presenter.getState().getScreenWidth(), presenter.getState()
                .getScreenHeight());
        presenter.draw();
    }

    public int getEditMinuteGradality() {
        return editMinuteGradality;
    }

    public void setEditMinuteGradality(int editMinuteGradality) {
        this.editMinuteGradality = editMinuteGradality;
    }

    public int getDisplayMinuteGradality() {
        return displayMinuteGradality;
    }

    public void setDisplayMinuteGradality(int displayMinuteGradality) {
        this.displayMinuteGradality = displayMinuteGradality;
    }
}
