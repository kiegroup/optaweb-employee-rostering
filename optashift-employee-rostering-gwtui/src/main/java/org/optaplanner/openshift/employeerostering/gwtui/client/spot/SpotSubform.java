package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.TextBox;
import elemental2.dom.HTMLTableCellElement;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.DataInvalidation;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.StringListToSkillSetConverter;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.TableRow;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

@Templated("#row")
public class SpotSubform extends TableRow<Spot> implements TakesValue<Spot> {

    @Inject
    private TenantStore tenantStore;

    @Inject
    private StringListToSkillSetConverter skillConvertor;

    @Inject
    @DataField("spot-name-text-box")
    private TextBox spotName;

    @Inject
    @DataField("spot-required-skill-set-select")
    private MultipleSelect spotRequiredSkillSet;

    @Inject
    @DataField("spot-name-display")
    @Named("td")
    private HTMLTableCellElement spotNameDisplay;

    @Inject
    @DataField("spot-required-skill-set-display")
    @Named("td")
    private HTMLTableCellElement spotRequiredSkillSetDisplay;

    @Inject
    private Event<DataInvalidation<Spot>> dataInvalidationEvent;

    @Inject
    private TranslationService translationService;

    private Subscription subscription;

    @Inject
    private CommonUtils commonUtils;

    @SuppressWarnings("unchecked")
    @PostConstruct
    protected void initWidget() {
        spotName.getElement().setAttribute("placeholder", translationService.format(
                OptaShiftUIConstants.SpotListPanel_spotName));
        dataBinder.getModel().setTenantId(tenantStore.getCurrentTenantId());
        updateSkillMap(skillConvertor.getSkillMap());
        dataBinder.bind(spotName, "name");
        dataBinder.bind(spotRequiredSkillSet, "requiredSkillSet", skillConvertor);

        dataBinder.<String> addPropertyChangeHandler("name", (e) -> {
            spotNameDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped(e.getNewValue()).toSafeHtml().asString();
        });
        dataBinder.<Set<Skill>> addPropertyChangeHandler("requiredSkillSet", (e) -> {
            spotRequiredSkillSetDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped(commonUtils.delimitCollection(e
                    .getNewValue(),
                    (s) -> s.getName(), ", ")).toSafeHtml().asString();
        });
        subscription = ErraiBus.get().subscribe("SkillMapListener", (m) -> updateSkillMap(m.get(Map.class, "Map")));
    }

    public void reset() {
        spotName.setValue("");
    }

    private void updateSkillMap(Map<String, Skill> skillMap) {
        spotRequiredSkillSet.clear();
        skillMap.forEach((name, skill) -> {
            Option option = new Option();
            option.setName(name);
            option.setValue(name);
            option.setText(name);
            spotRequiredSkillSet.add(option);
        });
        spotRequiredSkillSet.refresh();
    }

    @Override
    protected void deleteRow(Spot spot) {
        SpotRestServiceBuilder.removeSpot(tenantStore.getCurrentTenantId(), spot.getId(),
                FailureShownRestCallback.onSuccess(success -> {
                    dataInvalidationEvent.fire(new DataInvalidation<>());
                }));
    }

    @Override
    protected void updateRow(Spot oldValue, Spot newValue) {
        SpotRestServiceBuilder.updateSpot(tenantStore.getCurrentTenantId(), newValue,
                FailureShownRestCallback.onSuccess(v -> {
                    dataInvalidationEvent.fire(new DataInvalidation<>());
                }));
    }

    @Override
    protected void createRow(Spot spot) {
        SpotRestServiceBuilder.addSpot(tenantStore.getCurrentTenantId(), spot,
                FailureShownRestCallback.onSuccess(v -> {
                    dataInvalidationEvent.fire(new DataInvalidation<>());
                }));
    }

    @Override
    public void onUnload() {
        subscription.remove();
    }

    @Override
    protected void focusOnFirstInput() {
        spotName.setFocus(true);
    }
}
