package org.optaplanner.openshift.employeerostering.shared.roster.view;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.Indictment;

public class IndictmentView {

    private List<ConstraintMatchView> constraintMatchList;

    public IndictmentView() {

    }

    public IndictmentView(Indictment indictment) {
        constraintMatchList = new ArrayList<>(indictment.getConstraintMatchCount());

        for (ConstraintMatch cm : indictment.getConstraintMatchSet()) {
            constraintMatchList.add(new ConstraintMatchView(cm));
        }
    }

    public IndictmentView(List<ConstraintMatchView> constraintMatchList) {
        this.constraintMatchList = constraintMatchList;
    }

    public List<ConstraintMatchView> getConstraintMatchList() {
        return constraintMatchList;
    }

    public void setConstraintMatchList(List<ConstraintMatchView> constraintMatchList) {
        this.constraintMatchList = constraintMatchList;
    }
}
