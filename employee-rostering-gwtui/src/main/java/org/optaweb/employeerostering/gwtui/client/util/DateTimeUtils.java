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

package org.optaweb.employeerostering.gwtui.client.util;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.i18n.client.DateTimeFormat;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.optaweb.employeerostering.gwtui.client.common.MomentJs;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;
import org.optaweb.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;

@Singleton
public class DateTimeUtils {

    @Inject
    private TranslationService translationService;

    public String translateLocalDate(LocalDate localDate) {
        String dateFormatString = translationService.getTranslation(I18nKeys.LocalDate_format);
        DateTimeFormat dateFormat = DateTimeFormat.getFormat(dateFormatString);
        return dateFormat.format(GwtJavaTimeWorkaroundUtil.toDate(localDate.atTime(0, 0)));
    }

    public String translateLocalTime(LocalTime localTime) {
        String timeFormatString = translationService.getTranslation(I18nKeys.LocalTime_format);
        DateTimeFormat dateFormat = DateTimeFormat.getFormat(timeFormatString);
        return dateFormat.format(GwtJavaTimeWorkaroundUtil.toDate(LocalDate.now().atTime(localTime)));
    }

    public LocalDate getFirstDateOfWeek(LocalDate dayInWeek) {
        return LocalDate.parse(MomentJs.moment(dayInWeek.toString()).day(0).format("YYYY-MM-DD"));
    }

    public LocalDate getLastDateOfWeek(LocalDate dayInWeek) {
        return LocalDate.parse(MomentJs.moment(dayInWeek.toString()).day(6).format("YYYY-MM-DD"));
    }
}
