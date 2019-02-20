/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.pages.skill;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;
import elemental2.dom.HTMLTableCellElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.AutoTrimWhitespaceTextBox;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.TableRow;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.skill.Skill;
import org.optaweb.employeerostering.shared.skill.SkillRestServiceBuilder;

@Templated("#row")
public class SkillTableRow extends TableRow<Skill> implements TakesValue<Skill> {

    @Inject
    private TenantStore tenantStore;

    @Inject
    @DataField("skill-name-text-box")
    private AutoTrimWhitespaceTextBox skillName;

    @Inject
    @DataField("skill-name-display")
    @Named("td")
    private HTMLTableCellElement skillNameDisplay;

    @Inject
    private EventManager eventManager;

    @Inject
    private TranslationService translationService;

    @PostConstruct
    protected void initWidget() {
        skillName.getElement().setAttribute("placeholder", translationService.format(
                I18nKeys.SkillListPanel_skillName));
        dataBinder.getModel().setTenantId(tenantStore.getCurrentTenantId());
        dataBinder.bind(skillName, "name");

        dataBinder.<String>addPropertyChangeHandler("name", (e) -> {
            skillNameDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped(e.getNewValue()).toSafeHtml().asString();
        });
    }

    public void reset() {
        skillName.setValue("");
    }

    @Override
    protected void deleteRow(Skill skill) {
        SkillRestServiceBuilder.removeSkill(tenantStore.getCurrentTenantId(), skill.getId(),
                                            FailureShownRestCallback.onSuccess(success -> {
                                                eventManager.fireEvent(EventManager.Event.DATA_INVALIDATION, Skill.class);
                                            }));
    }

    @Override
    protected void updateRow(Skill oldValue, Skill newValue) {
        SkillRestServiceBuilder.updateSkill(tenantStore.getCurrentTenantId(), newValue,
                                            FailureShownRestCallback.onSuccess(v -> {
                                                eventManager.fireEvent(EventManager.Event.DATA_INVALIDATION, Skill.class);
                                            }));
    }

    @Override
    protected void createRow(Skill skill) {
        SkillRestServiceBuilder.addSkill(tenantStore.getCurrentTenantId(), skill,
                                         FailureShownRestCallback.onSuccess(v -> {
                                             eventManager.fireEvent(EventManager.Event.DATA_INVALIDATION, Skill.class);
                                         }));
    }

    @Override
    protected void focusOnFirstInput() {
        skillName.setFocus(true);
    }
}
