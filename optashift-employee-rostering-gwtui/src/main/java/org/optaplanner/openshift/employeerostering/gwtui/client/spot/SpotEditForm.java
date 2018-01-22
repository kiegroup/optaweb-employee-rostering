package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.extras.tagsinput.client.ui.base.SingleValueTagsInput;
import org.gwtbootstrap3.extras.typeahead.client.base.CollectionDataset;
import org.gwtbootstrap3.extras.typeahead.client.base.Dataset;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.FormPopup;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

@Templated
public class SpotEditForm implements IsElement {

    @Inject
    @DataField
    private TextBox spotName;

    @Inject
    @DataField
    private SingleValueTagsInput<Skill> requiredSkills;

    @Inject
    @DataField
    private Button saveButton;

    @Inject
    @DataField
    private Button cancelButton;

    @Inject
    @DataField
    private Button closeButton;

    @Inject
    @DataField
    private @Named(value = "h3") HeadingElement title;

    @Inject
    private TranslationService CONSTANTS;

    private static Spot spot;
    private static SpotListPanel panel;
    private static List<Skill> skillList;

    private FormPopup popup;

    public static SpotEditForm create(SyncBeanManager beanManager, SpotListPanel spotPanel, Spot spotData, List<
            Skill> skillData) {
        panel = spotPanel;
        spot = spotData;
        skillList = skillData;
        return beanManager.lookupBean(SpotEditForm.class).newInstance();
    }

    @PostConstruct
    protected void initWidget() {
        spotName.setValue(spot.getName());
        requiredSkills.removeAll();
        CollectionDataset<Skill> data = new CollectionDataset<Skill>(skillList) {

            @Override
            public String getValue(Skill skill) {
                return (skill == null) ? "" : skill.getName();
            }
        };
        requiredSkills.setDatasets((Dataset<Skill>) data);
        requiredSkills.setItemValue(Skill::getName);
        requiredSkills.setItemText(Skill::getName);
        requiredSkills.reconfigure();
        requiredSkills.add(spot.getRequiredSkillSet().stream().collect(Collectors.toList()));
        title.setInnerSafeHtml(new SafeHtmlBuilder().appendEscaped(spot.getName())
                .toSafeHtml());
        popup = FormPopup.getFormPopup(this);
        popup.center();
    }

    @EventHandler("cancelButton")
    public void cancel(ClickEvent e) {
        popup.hide();
    }

    @EventHandler("closeButton")
    public void close(ClickEvent e) {
        popup.hide();
    }

    @EventHandler("saveButton")
    public void save(ClickEvent click) {
        spot.setName(spotName.getValue());
        spot.setRequiredSkillSet(new HashSet<>(requiredSkills.getItems()));
        popup.hide();
        SpotRestServiceBuilder.updateSpot(spot.getTenantId(), spot, new FailureShownRestCallback<Spot>() {

            @Override
            public void onSuccess(Spot spot) {
                panel.refresh();
            }
        });

    }

}
