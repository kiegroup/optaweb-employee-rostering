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
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import MockDate from 'mockdate';
import moment from 'moment';

configure({ adapter: new Adapter() });

const mockDate = moment("2018-01-01", "YYYY-MM-DD").toDate();
MockDate.set(mockDate);

const mockTranslate = jest.fn().mockImplementation(k => `Trans(i18nKey=${k})`);
export { mockTranslate };
jest.mock('react-i18next', () => ({
  // this mock makes sure any components using the translate HoC receive the t function as a prop
  withTranslation: () => (Component: any) => {
    Component.defaultProps = { ...Component.defaultProps, t: mockTranslate, tReady: true };
    return () => Component;
  },

  useTranslation: () => ({ t: mockTranslate }),
  Trans: jest.requireActual("react-i18next").Trans
}));