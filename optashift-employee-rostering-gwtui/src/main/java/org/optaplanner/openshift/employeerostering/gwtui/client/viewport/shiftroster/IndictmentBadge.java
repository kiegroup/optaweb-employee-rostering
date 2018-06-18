package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.shiftroster;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLElement;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public class IndictmentBadge implements IsElement {

    @Inject
    @DataField("icon")
    @Named("span")
    private HTMLElement icon;

    @Inject
    @DataField("content")
    @Named("span")
    private HTMLElement content;

    private List<String> constraintMatchTooltipList;
    
    private final static int MAX_TOOLTIP_LENGTH = 200;

    @PostConstruct
    public void init() {
        constraintMatchTooltipList = new ArrayList<>();
    }

    public HTMLElement getIcon() {
        return icon;
    }

    public void clear() {
        constraintMatchTooltipList.clear();
        getElement().classList.add("hidden");
    }

    public void addConstraintMatch(String tooltip) {
        getElement().classList.remove("hidden");
        constraintMatchTooltipList.add(tooltip);
        content.innerHTML = constraintMatchTooltipList.size() + "";
        StringBuilder badgeTooltip = new StringBuilder();
        constraintMatchTooltipList.forEach(tt -> badgeTooltip.append(tt).append("\n"));
        if (badgeTooltip.length() > MAX_TOOLTIP_LENGTH - 3) {
            badgeTooltip.setLength(MAX_TOOLTIP_LENGTH - 3);
            badgeTooltip.append("...");
        }
        getElement().title = badgeTooltip.toString();
    }
}
