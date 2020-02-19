/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
import i18n from 'i18next';
import Backend from 'i18next-xhr-backend';
import LanguageDetector from 'i18next-browser-languagedetector';
import { initReactI18next } from 'react-i18next';
import { registerLocale, setDefaultLocale } from 'react-datepicker';
import cmn from 'date-fns/locale/zh-CN';
import YAML from 'yaml';

import moment from 'moment';
import 'moment/min/locales.min';

registerLocale('cmn', cmn);

function languageToMomentLocale(lang: string): string {
  switch (lang) {
    case 'cmn':
      return 'zh-cn';
    default:
      return lang;
  }
}

// From https://github.com/i18next/react-i18next/blob/master/example/react/src/i18n.js
i18n
  // load translation using xhr -> see /public/locales
  // learn more: https://github.com/i18next/i18next-xhr-backend
  .use(Backend)
  // detect user language
  // learn more: https://github.com/i18next/i18next-browser-languageDetector
  .use(LanguageDetector)
  // pass the i18n instance to react-i18next.
  .use(initReactI18next)
  // init i18next
  // for all options read: https://www.i18next.com/overview/configuration-options
  .init({
    fallbackLng: 'en',
    fallbackNS: 'Common',
    defaultNS: 'Common',
    // Load all namespaces on boot so users don't see an empty screen
    ns: [
      'Alerts',
      'AvailabilityRosterPage',
      'Common',
      'ContractsPage',
      'EditAvailabilityModal',
      'EditShiftModal',
      'EditShiftTemplateModal',
      'EmployeesPage',
      'Navigation',
      'NewTenantFormModal',
      'RestServiceClient',
      'RotationPage',
      'ServerSideException',
      'ShiftEvent',
      'ShiftRosterPage',
      'SpotsPage',
    ],
    debug: true,
    backend: {
      loadPath: '/assets/translations/{{lng}}/{{ns}}.yaml',
      parse: (d: string) => YAML.parseDocument(d).toJSON(),
    },
    interpolation: {
      escapeValue: false, // not needed for react as it escapes by default
    },
  }, () => {
    moment.locale(languageToMomentLocale(i18n.language));
    setDefaultLocale(i18n.language);
  });

export default i18n;
