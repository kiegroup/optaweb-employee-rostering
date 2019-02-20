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

package org.optaweb.employeerostering.gwtui.client.tenant;

import java.time.LocalDate;
import java.time.ZoneId;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import elemental2.dom.Event;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MouseEvent;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.LocalDatePicker;
import org.optaweb.employeerostering.gwtui.client.popups.FormPopup;
import org.optaweb.employeerostering.gwtui.client.popups.PopupFactory;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.tenant.Tenant;
import org.optaweb.employeerostering.shared.tenant.TenantRestServiceBuilder;

@Templated
public class NewTenantForm implements IsElement {

    @Inject
    @DataField("tenant-name-text-box")
    private HTMLInputElement tenantName;

    @Inject
    @DataField("schedule-start-date")
    private LocalDatePicker scheduleStartDate;

    @Inject
    @DataField("schedule-start-weekday")
    private HTMLDivElement scheduleStartWeekday;

    @Inject
    @DataField("publish-notice")
    private HTMLInputElement publishNotice;

    @Inject
    @DataField("publish-length")
    private HTMLInputElement publishLength;

    @Inject
    @DataField("rotation-length")
    private HTMLInputElement rotationLength;

    @Inject
    @DataField("draft-length")
    private HTMLInputElement draftLength;

    @Inject
    @DataField("timezone-select")
    private Select timezoneSelect;

    @Inject
    @DataField("save-button")
    private HTMLButtonElement saveTenantButton;

    @Inject
    @DataField("close-button")
    private HTMLButtonElement closeButton;

    @Inject
    @DataField("cancel-button")
    private HTMLButtonElement cancelButton;

    @Inject
    private PopupFactory popupFactory;

    @Inject
    private EventManager eventManager;

    private FormPopup formPopup;

    @PostConstruct
    public void init() {
        scheduleStartDate.setValue(LocalDate.now());
        publishNotice.valueAsNumber = 7;
        publishLength.valueAsNumber = 7;
        rotationLength.valueAsNumber = 28;
        draftLength.valueAsNumber = 21;

        TenantRestServiceBuilder.getSupportedTimezones(FailureShownRestCallback.onSuccess(timezoneList -> {
            String systemTimezone = getSystemTimezone();
            for (ZoneId timezone : timezoneList) {
                Option option = new Option();
                option.setText(timezone.getId());
                option.setName(timezone.getId());
                option.setValue(timezone.getId());
                if (option.getValue().equals(systemTimezone)) {
                    option.setSelected(true);
                }
                timezoneSelect.add(option);
            }
            timezoneSelect.refresh();
            formPopup = popupFactory.getFormPopup(this).get();
            formPopup.center(600, 500);
        }));

        scheduleStartWeekday.innerHTML = "Schedule will begin on " + LocalDate.now().getDayOfWeek() + ".";
        scheduleStartDate.addValueChangeHandler(e -> {
            scheduleStartWeekday.innerHTML = "Schedule will begin on " + e.getValue().getDayOfWeek().toString() + ".";
        });
    }

    public boolean isValid() {
        return tenantName.reportValidity() &&
                scheduleStartDate.reportValidity() &&
                publishNotice.reportValidity() &&
                publishLength.reportValidity() &&
                rotationLength.reportValidity() &&
                draftLength.reportValidity() &&
                timezoneSelect.getSelectedItem() != null;
    }

    @EventHandler("cancel-button")
    public void onCancelButtonClick(@ForEvent("click") final MouseEvent e) {
        e.stopPropagation();
        formPopup.hide();
    }

    @EventHandler("close-button")
    public void onCloseButtonClick(@ForEvent("click") final MouseEvent e) {
        e.stopPropagation();
        formPopup.hide();
    }

    @EventHandler("save-button")
    public void onApplyButtonClick(@ForEvent("click") final MouseEvent e) {
        e.stopPropagation();
        if (isValid()) {
            Tenant newTenant = new Tenant(tenantName.value);

            RosterState rosterState = new RosterState();
            rosterState.setFirstDraftDate(scheduleStartDate.getValue());
            rosterState.setLastHistoricDate(scheduleStartDate.getValue().minusDays(1));
            // publishLength is read-only and is set to 7
            // rosterState.setPublishLength(valueAsInt(publishLength));
            rosterState.setPublishNotice(valueAsInt(publishNotice));
            rosterState.setDraftLength((valueAsInt(draftLength)));
            rosterState.setRotationLength((valueAsInt(rotationLength)));
            rosterState.setUnplannedRotationOffset(0);
            rosterState.setTimeZone(ZoneId.of(timezoneSelect.getSelectedItem().getValue()));
            rosterState.setTenant(newTenant);

            TenantRestServiceBuilder.addTenant(rosterState, FailureShownRestCallback.onSuccess(tenant -> {
                eventManager.fireEvent(EventManager.Event.DATA_INVALIDATION, Tenant.class);
                formPopup.hide();
            }));
        }
    }

    // Exploits event bubbling
    @EventHandler("form")
    public void onFormInputChange(@ForEvent("change") Event e) {
        saveTenantButton.disabled = !isValid();
    }

    private static int valueAsInt(HTMLInputElement inputElement) {
        return (int) Math.round(inputElement.valueAsNumber);
    }

    private static native String getSystemTimezone() /*-{
        return Intl.DateTimeFormat().resolvedOptions().timeZone;
    }-*/;
}
